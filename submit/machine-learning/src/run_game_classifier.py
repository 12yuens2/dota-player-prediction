import os
import sys

import classifiers
import run_maps

from game import Game


def contains_player(game, filter_id):
    if filter_id in game.csv_file:
        return 1
    else:
        return 0


def load_games(filepath):
    games = []
    for filename in os.listdir(filepath):
        games.append(Game("{}/{}".format(filepath, filename)))

    return games



#path = "/cs/scratch/sy35/dota-data/{}/data/mouseaction".format(hero_id)
path = sys.argv[1]
cv = int(sys.argv[2])
filter_id = int(sys.argv[3])
games = load_games(path)

models = [run_maps.rf_model(run_maps.rf_stats_map, "mouse", None)]
with open("results.csv", "w") as f:
    f.write("accuracy,precision,recall,model,features\n")
    f.flush()

    for cv in range(2, 11):
        for model_name,model_map,features,network_size in models:
            print("Running {} on {} features".format(model_name, features))

            classifier = classifiers.GameClassifier(filter_id, model_map, network_size)
            score_map = classifiers.cross_validate(games, cv, classifier, classifier.contains_player)

            for feature,score in score_map.items():
                accuracy = score_map[feature]["accuracy"]
                precision = score_map[feature]["precision"]
                recall = score_map[feature]["recall"]

                f.write("{},{},{},{},{},{}\n".format(accuracy,precision,recall,model_name,features,cv))
                f.flush()
