import ntpath
import os
import random
import sys

import pandas as pd

from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier

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


lr_mc = classifiers.MoveClassifier(filter_id, lr_model_map)
xs = ["{}/{}".format(path, file) for file in os.listdir(path)]

classifiers.cross_validate(xs, cv, lr_mc, lr_mc._get_ys)
#data.extend(lr_mc.cross_validate(5))
#
#rf_mc = MoveClassifier(filter_id, rf_model_map, path)
#data.extend(rf_mc.cross_validate(5))
#
#with open("out.data", "w") as f:
#    f.write("move_type,model,eval_type,accuracy,precision,recall\n")
#    for line in data:
#        f.write(line)
#    f.flush()


                    

