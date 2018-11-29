import os
import sys
import classifiers

from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier

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
    "ATTACK": RandomForestClassifier(class_weight="balanced"),
    "MOVE": RandomForestClassifier(class_weight="balanced"),
    "CAST": RandomForestClassifier(class_weight="balanced")
}

rf_stats_map = {
    "STATS": RandomForestClassifier(class_weight="balanced")
}

rf_move_stats = {**rf_move_map, **rf_stats_map}

mlp_move_map = {
    "ATTACK": MLPClassifier(alpha=0.001),
    "MOVE": MLPClassifier(alpha=0.001),
    "CAST": MLPClassifier(alpha=0.001)
}

mlp_stats_map = {
    "STATS": MLPClassifier(hidden_layer_sizes=(10,), alpha=0.001)
}

mlp_move_stats = {**mlp_move_map, **mlp_stats_map}

models = [
    ("Logistic Regression", lr_move_map, "mouse"), ("Random Forest", rf_move_map, "mouse"), ("Multi-layer Perceptron", mlp_move_map, "mouse"),
    ("Logistic Regression", lr_stats_map, "stats"), ("Random Forest", rf_stats_map, "stats"), ("Multi-layer Perceptron", mlp_stats_map, "stats"),
    ("Logistic Regression", lr_move_stats, "mouse-stats"), ("Random Forest", rf_move_stats, "mouse-stats"), ("Multi-layer Perceptron", mlp_move_stats, "mouse-stats")
]

with open("15-game-cv.csv", "w") as f:
    f.write("accuracy,precision,recall,model,features\n")
    f.flush()

    for model_name,model_map,features in models:
        classifier = classifiers.GameClassifier(filter_id, model_map, (3,))
        score_map = classifiers.cross_validate(games, cv, classifier, classifier.contains_player)

        for feature,score in score_map.items():
            accuracy = score_map[feature]["accuracy"]
            precision = score_map[feature]["precision"]
            recall = score_map[feature]["recall"]

            f.write("{},{},{},{},{}\n".format(accuracy,precision,recall,model_name,features))
            f.flush()

#    lr_gc = classifiers.GameClassifier(filter_id, lr_map, (3,), "../results/15-game-move-lr-cv.csv")
#    classifiers.cross_validate(games, cv, lr_gc, lr_gc.contains_player, f, "Logistic Regression")
#
#    rf_gc = classifiers.GameClassifier(filter_id, rf_map, (3,), "../results/15-game-move-rf-cv.csv")
#    classifiers.cross_validate(games, cv, rf_gc, rf_gc.contains_player, f, "Random Forest")
#
#    mlp_gc = classifiers.GameClassifier(filter_id, mlp_map, (3,), "../results/15-game-move-mlp-cv.csv")
#    classifiers.cross_validate(games, cv, mlp_gc, mlp_gc.contains_player, f, "Multi-layer Perceptron")


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
