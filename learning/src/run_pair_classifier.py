from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score

from sklearn.linear_model import LogisticRegression 
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.model_selection import StratifiedKFold

from pair_classifier import PairClassifier


def ml(X, y, pc, cv, split_num):
    skf = StratifiedKFold(cv)

    for train_index, test_index in skf.split(X, y):
        X_train = [X[i] for i in train_index]
        X_test = [X[i] for i in test_index]
        y_train = [y[i] for i in train_index]
        y_test = [y[i] for i in test_index]

        pc.train(X_train, y_train, split_num)
        pc.test(X_test, y_test, split_num)

        
def ml_split(X, y, model, cv, splits):
    for i in range(splits):
        print("split {}".format(i))
        ml(X, y, model, cv, i)
        
    print("all")
    ml(X, y, model, cv, -1)


network_size = (3,)

rf_map = {
    "ATTACK": RandomForestClassifier(class_weight="balanced"),
    "MOVE": RandomForestClassifier(class_weight="balanced"),
    "CAST": RandomForestClassifier(class_weight="balanced"),
    "STATS": RandomForestClassifier(class_weight="balanced")
}

clf_map = {
    "ATTACK": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,)),
    "MOVE": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,)),
    "CAST": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,)),
    "STATS": MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(10,))
}

rf = PairClassifier(rf_map, network_size)
clf = PairClassifier(clf_map, network_size)

#clfs = PairClassifier(MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,16,)),
#                      MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,16,)),
#                      MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(256,16,)),
#                      MLPClassifier(solver="lbfgs", alpha=0.001, hidden_layer_sizes=(10,)),
#                      network_size)

def run_models(name, pairs, y, size):
#    print("lr {}".format(name))
#    ml_split(pairs, y, lr, 5, size)

    print("rf {}".format(name))
    ml_split(pairs, y, rf, 5, size)

    print("mlp {}".format(name))
    ml_split(pairs, y, clf, 5, size)

#    print("mlp small {}".format(name))
#    ml_split(pairs, y, clfs, 5, size)


def get_ys(pairs):
    return [pair.y for pair in pairs]

import sys
import pickle

hero_id = sys.argv[1]
path = "/cs/scratch/sy35/dota-data/{}/dfs".format(hero_id)

pairs1 = pickle.load(open("{}/pairs1.df".format(path), "rb"))
run_models("pairs1", pairs1, get_ys(pairs1), 1)

pairs2 = pickle.load(open("{}/pairs2.df".format(path), "rb"))
run_models("pairs2", pairs2, get_ys(pairs2), 2)

pairs3 = pickle.load(open("{}/pairs3.df".format(path), "rb"))
run_models("pairs3", pairs3, get_ys(pairs3), 3)

#pairs5 = pickle.load(open("{}/pairs5.df".format(path), "rb"))
#run_models("pairs5", pairs5, get_ys(pairs5), 5)

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


