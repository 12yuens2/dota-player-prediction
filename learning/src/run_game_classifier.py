import os
import sys
import classifiers

from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier

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


filter_id = 76561198047065028
#hero_id = int(sys.argv[1])
#path = "/cs/scratch/sy35/dota-data/{}/data/mouseaction".format(hero_id)
path = sys.argv[1]
cv = int(sys.argv[2])
filter_id = int(sys.argv[3])
games = load_games(path)

lr_map = {
    "ATTACK": LogisticRegression(class_weight="balanced"),
    "MOVE": LogisticRegression(class_weight="balanced"),
    "CAST": LogisticRegression(class_weight="balanced")
}

rf_map = {
    "ATTACK": RandomForestClassifier(class_weight="balanced"),
    "MOVE": RandomForestClassifier(class_weight="balanced"),
    "CAST": RandomForestClassifier(class_weight="balanced")
}

lr_gc = classifiers.GameClassifier(filter_id, lr_map, (3,), "../results/15-game-move-lr-cv.csv")
classifiers.cross_validate(games, cv, lr_gc, lr_gc.contains_player)

rf_gc = classifiers.GameClassifier(filter_id, rf_map, (3,), "../results/15-game-move-rf-cv.csv")
classifiers.cross_validate(games, cv, rf_gc, rf_gc.contains_player)


#lr_gc.train(games, 
#data.extend(lr_gc.cross_validate(games, 5))

#rf_gc = GameClassifier(filter_id, rf_model_map, (3,))
#data.extend(rf_gc.cross_validate(games, 5))
#
#rf_stats_gc = GameClassifier(filter_id, rf_stats_model_map, (3,))
#data.extend(rf_stats_gc.cross_validate(games, 5))

#with open("out2.data", "w") as f:
#    f.write("move_type,model,eval_type,accuracy,precision,recall\n")
#    for line in data:
#        f.write(line)
#    f.flush()
