import ntpath
import os
import random

from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from move_classifier import MoveClassifier


def ml(path, splits, class_weight, filter_id):
    csvs = ["{}/{}".format(path,file) for file in os.listdir(path)]
    
    dfs = [pd.read_csv(csv_file) for csv_file in csvs]
    ys = [is_filter_player(filter_id, ntpath.basename(csv_file)[:17]) for csv_file in csvs]
    
    cross_validate(dfs, ys, splits, filter_id, class_weight)



filter_id = 76561198047065028
path = "/cs/scratch/sy35/dota-data/15/data/mouseaction"

#path = sys.argv[1]
#filter_id = int(sys.argv[2])
lr_model_map = {
    "ATTACK": LogisticRegression(class_weight="balanced"),
    "MOVE": LogisticRegression(class_weight="balanced"),
    "CAST": LogisticRegression(class_weight="balanced")
}

rf_model_map = {
    "ATTACK": RandomForestClassifier(class_weight="balanced"),
    "MOVE": RandomForestClassifier(class_weight="balanced"),
    "CAST": RandomForestClassifier(class_weight="balanced")
}

data = []

lr_mc = MoveClassifier(filter_id, lr_model_map, path)
data.extend(lr_mc.cross_validate(5))

rf_mc = MoveClassifier(fitler_id, rf_model_map, path)
data.extend(rf_mc.cross_validate(5))

with open("out.data", "w") as f:
    f.write("move_type,model,eval_type,accuracy,precision,recall\n")
    for line in data:
        f.write(line)
    f.flush()


                    

