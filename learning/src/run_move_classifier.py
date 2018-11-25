import ntpath
import os
import random
import sys

import pandas as pd

from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier

import classifiers

def get_y(csv_file):
    return is_filter_player(self.filter_id, ntpath.basename(csv_file)[:17])

#filter_id = 76561198030654385
#path = "/cs/scratch/sy35/dota-data/13/data/train/mouseaction"

path = sys.argv[1]
cv = int(sys.argv[2])
filter_id = int(sys.argv[3])

lr_model_map = {
    "ATTACK": LogisticRegression(class_weight="balanced"),
    "MOVE": LogisticRegression(class_weight="balanced"),
    "CAST": LogisticRegression(class_weight="balanced")
}
#76561198066839629
rf_model_map = {
    "ATTACK": RandomForestClassifier(class_weight="balanced"),
    "MOVE": RandomForestClassifier(class_weight="balanced"),
    "CAST": RandomForestClassifier(class_weight="balanced")
}

mlp_model_map = {
    "ATTACK": MLPClassifier(alpha=0.001),
    "MOVE": MLPClassifier(alpha=0.001),
    "CAST": MLPClassifier(alpha=0.001)
}


lr_mc = classifiers.MoveClassifier(filter_id, lr_model_map, "../results/15-move-lr-cv.csv")
rf_mc = classifiers.MoveClassifier(filter_id, rf_model_map, "../results/15-move-rf-cv.csv")
mlp_mc = classifiers.MoveClassifier(filter_id, mlp_model_map, "../results/15-move-mlp-cv.csv")


xs = ["{}/{}".format(path, file) for file in os.listdir(path)]

print("Logistic Regression")
classifiers.cross_validate(xs, cv, lr_mc, lr_mc._get_ys)

print("Random Forest")
classifiers.cross_validate(xs, cv, rf_mc, rf_mc._get_ys)

print("MLP")
classifiers.cross_validate(xs, cv, mlp_mc, mlp_mc._get_ys)
