import classifiers
import run_maps

from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score

from sklearn.linear_model import LogisticRegression 
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier


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

models = run_maps.models
with open(output_path, "w") as f:
    f.write("numSplits,split,accuracy,precision,recall,model\n")
    f.flush()

    for splits in [1,2,3,5]:
        run_pairs(splits, models, f)


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

