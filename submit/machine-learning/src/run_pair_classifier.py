import classifiers
import run_maps
import sys
import pickle

from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score

from sklearn.linear_model import LogisticRegression 
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier


def get_y(pair):
    return pair.y


def run_pairs(pair_num, models, output):
    pairs = pickle.load(open("{}/pairs{}.df".format(path, pair_num), "rb"))

    for model_name,model_map,features,network_size in models:
        limit = pair_num + 1
        if "mouse" not in features:
            limit = 2

        for split_num in range(1, limit):
            print("{}, {}".format(model_name, features))

            classifier = classifiers.PairClassifier(model_map, network_size)
            score_map = classifiers.cross_validate(pairs, cv, classifier, get_y, split_num)

            for feature,score in score_map.items():
                accuracy = score_map[feature]["accuracy"]
                precision = score_map[feature]["precision"]
                recall = score_map[feature]["recall"]

                output.write("{},{},{},{},{},{},{}\n".format(pairs[0].splits, split_num,
                                                             accuracy,precision,recall,model_name,features))
                output.flush()


#path = "/cs/scratch/sy35/dota-data/{}/dfs".format(hero_id)
path = sys.argv[1]
cv = int(sys.argv[2])
output_path = sys.argv[3]

models = run_maps.models
models.extend([
    run_maps.lr_model(run_maps.lr_items_difference, "items-diff", None),
    run_maps.lr_model(run_maps.lr_move_items_diff, "mouse-items-diff"),
    run_maps.lr_model(run_maps.lr_stats_items_diff, "stats-items-diff"),
    run_maps.lr_model(run_maps.lr_move_stats_items_diff, "mouse-stats-items-diff"),

    run_maps.rf_model(run_maps.rf_items_difference, "items-diff", None),
    run_maps.rf_model(run_maps.rf_move_items_diff, "mouse-items-diff"),
    run_maps.rf_model(run_maps.rf_stats_items_diff, "stats-items-diff"),
    run_maps.rf_model(run_maps.rf_move_stats_items_diff, "mouse-stats-items-diff"),

    run_maps.mlp_model(run_maps.mlp_items_difference, "items-diff", None),
    run_maps.mlp_model(run_maps.mlp_move_items_diff, "mouse-items-diff"),
    run_maps.mlp_model(run_maps.mlp_stats_items_diff, "stats-items-diff"),
    run_maps.mlp_model(run_maps.mlp_move_stats_items_diff, "mouse-stats-items-diff"),
])

with open(output_path, "w") as f:
    f.write("numSplits,split,accuracy,precision,recall,model,features\n")
    f.flush()

    for splits in [1,2,3,5]:
        run_pairs(splits, models, f)

