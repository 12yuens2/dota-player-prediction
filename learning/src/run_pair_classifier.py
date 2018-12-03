import classifiers

from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score

from sklearn.linear_model import LogisticRegression 
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier

network_size = (3,)

lr_move_map = {
    "ATTACK": LogisticRegression(class_weight="balanced"),
    "MOVE": LogisticRegression(class_weight="balanced"),
    "CAST": LogisticRegression(class_weight="balanced")
}

lr_stats_map = {
    "STATS": LogisticRegression(class_weight="balanced")
}

lr_move_stats = {**lr_move_map, **lr_stats_map}

rf_move_map = {
#    "START_ITEMS": RandomForestClassifier(class_weight="balanced"),
#    "END_ITEMS": RandomForestClassifier(class_weight="balanced"),
    "ATTACK": RandomForestClassifier(class_weight="balanced"),
    "MOVE": RandomForestClassifier(class_weight="balanced"),
    "CAST": RandomForestClassifier(class_weight="balanced")
#    "STATS": RandomForestClassifier(class_weight="balanced")
}

rf_stats_map = {
    "STATS": RandomForestClassifier(class_weight="balanced")
}

rf_move_stats = {**rf_move_map, **rf_stats_map}

clf_move_map = {
    #"START_ITEMS": MLPClassifier(solver='lbfgs', alpha=0.001, hidden_layer_sizes=(10,)),
    #"END_ITEMS": MLPClassifier(solver='lbfgs', alpha=0.001, hidden_layer_sizes=(10,)),
    "ATTACK": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,)),
    "MOVE": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,)),
    "CAST": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,))
    #"STATS": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(10,))
}

clf_stats_map = {
    "STATS": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(10,))
}

clf_move_stats = {**clf_move_map, **clf_stats_map}

#lr = classifiers.PairClassifier(lr_map, network_size, "../results/15-pair-move-lr-cv.csv")
#rf = classifiers.PairClassifier(rf_map, network_size, "../results/15-pair-move-rf-cv.csv")
#clf = classifiers.PairClassifier(clf_map, network_size, "../results/15-pair-move-mlp-cv.csv")
models = [
    ("Logistic Regression", lr_move_map, "mouse"), ("Random Forest", rf_move_map, "mouse"), ("Multi-layer Perceptron", clf_move_map, "mouse"),
    ("Logistic Regression", lr_stats_map, "stats"), ("Random Forest", rf_stats_map, "stats"), ("Multi-layer Perceptron", clf_stats_map, "stats"),
    ("Logistic Regression", lr_move_stats, "mouse-stats"), ("Random Forest", rf_move_stats, "mouse-stats"), ("Multi-layer Perceptron", clf_move_stats, "mouse-stats")
]


def get_y(pair):
    return pair.y


import sys
import pickle

def run_pairs(pair_num, models, output):
    pairs = pickle.load(open("{}/pairs{}.df".format(path, pair_num), "rb"))

    for model_name,model_map,features in models:
        for split_num in range(1, pair_num+1):
            classifier = classifiers.PairClassifier(model_map, (3,))
            score_map = classifiers.cross_validate(pairs, cv, classifier, get_y, split_num)

            for feature,score in score_map.items():
                accuracy = score_map[feature]["accuracy"]
                precision = score_map[feature]["precision"]
                recall = score_map[feature]["recall"]

                output.write("{},{},{},{},{},{},{}\n".format(pairs[0].splits, split_num,
                                                             accuracy,precision,recall,model_name,features))
                output.flush()


#hero_id = sys.argv[1]
#path = "/cs/scratch/sy35/dota-data/{}/dfs".format(hero_id)
path = sys.argv[1]
cv = int(sys.argv[2])
output_path = sys.argv[3]

with open(output_path, "w") as f:
    f.write("numSplits,split,accuracy,precision,recall,model\n")
    f.flush()

    for splits in [1,2,3,5]:
        run_pairs(splits, models, f)

#    pairs1 = pickle.load(open("{}/pairs1.df".format(path), "rb"))
#    for model_name,model in models:
#        classifiers.cross_validate(pairs1, cv, model, get_y, f, model_name, 1)
#
#    pairs2 = pickle.load(open("{}/pairs2.df".format(path), "rb"))
#    for model_name,model in models:
#        classifiers.cross_validate(pairs2, cv, model, get_y, f, model_name, 1)
#        classifiers.cross_validate(pairs2, cv, model, get_y, f, model_name, 2)
#
#    pairs3 = pickle.load(open("{}/pairs3.df".format(path), "rb"))
#    for model_name,model in models:
#        classifiers.cross_validate(pairs3, cv, model, get_y, f, model_name, 1)
#        classifiers.cross_validate(pairs3, cv, model, get_y, f, model_name, 2)
#        classifiers.cross_validate(pairs3, cv, model, get_y, f, model_name, 3)
#
#    pairs5 = pickle.load(open("{}/pairs5.df".format(path), "rb"))
#    for model_name,model in models:
#        classifiers.cross_validate(pairs5, cv, model, get_y, f, model_name, 5)


#from sklearn.pipeline import Pipeline
#from sklearn.decomposition import PCA
#
#prepipe = Pipeline([
#    ("reduce_dimension", PCA()),
#    ("standardisation", StandardScaler())
#])
#
#lr = LogisticRegression(class_weight="balanced")
#rf = RandomForestClassifier(class_weight="balanced")
#nn = MLPClassifier(solver="lbfgs", alpha=0.001)
#
#lr_std = Pipeline([
#    ("standardisation", StandardScaler()),
#    ("classifier", lr)
#])
#
#rf_std = Pipeline([
#    ("standardisation", StandardScaler()),
#    ("classifier", rf)
#
#])
#
#nn_std = Pipeline([
#    ("standardisation", StandardScaler()),
#    ("classifier", nn)
#])
#
#clf_lr = Pipeline([
#    ("preprocessing", prepipe),
#    ("classifier", lr)
#])
#
#clf_rf = Pipeline([
#    ("preprocessing", prepipe),
#    ("classifier", rf)
#])
#
#clf_nn = Pipeline([
#    ("preprocessing", prepipe),
#    ("classifier", nn)
#])
#
## Logistic Regression
#ml_skf(5, X, y, lr)
#ml_skf(5, X, y, lr_std)
#ml_skf(5, X, y, clf_lr)
#
## Random Forest
#ml_skf(5, X, y, rf)
#ml_skf(5, X, y, rf_std)
#ml_skf(5, X, y, clf_rf)
#
## Neural Net
#ml_skf(5, X, y, nn)
#ml_skf(5, X, y, nn_std)
#ml_skf(5, X, y, clf_nn)


