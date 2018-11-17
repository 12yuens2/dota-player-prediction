import os
import ntpath
import pandas as pd

from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score

def get_action_df(raw_df, action):
    return raw_df.loc[raw_df["actionType"] == action].drop("actionType", 1)


def is_filter_player(steam_id, filter_id):
    if steam_id == filter_id:
        return 1
    else:
        return 0


def get_dfs(dfs):
    raw_df = pd.concat(dfs, ignore_index=True)
    raw_df = raw_df.dropna()
    
    attack_df = get_action_df(raw_df, "ATTACK")
    move_df = get_action_df(raw_df, "MOVE")
    cast_df = get_action_df(raw_df, "CAST")
    
    return attack_df, move_df, cast_df


class MoveClassifier():
    
    def __init__(self, filter_id, model_map, path):
        self.filter_id = filter_id
        self.models = model_map

        X, y = self._get_data(path)
        self.dfs = X
        self.ys = y


    def _get_data(self, path):
        csvs = ["{}/{}".format(path,f) for f in os.listdir(path)]

        dfs = [pd.read_csv(csv_file) for csv_file in csvs]
        ys = [is_filter_player(self.filter_id, ntpath.basename(csv_file)[:17]) for csv_file in csvs]

        return dfs, ys


    def train(self):
        attack_df, move_df, cast_df = get_dfs(self.dfs)
        
        self._fit_model("ATTACK", attack_df)
        self._fit_model("MOVE", move_df)
        self._fit_model("CAST", cast_df)

    
    def test(self, test_path):
        X, y = self._get_data(test_path)

        attack_df, move_df, cast_df = get_dfs(X)
        self._test_model("ATTACK", attack_df)
        
        

    def cross_validate(self, cv_splits, output_file=None):
        skf = StratifiedKFold(n_splits=cv_splits)

        data = []
        for train, test in skf.split(self.dfs, self.ys):
            training_dfs = [self.dfs[i] for i in train]
            testing_dfs = [self.dfs[i] for i in test]

            training_attack_df, training_move_df, training_cast_df = get_dfs(training_dfs)
            testing_attack_df, testing_move_df, testing_cast_df = get_dfs(testing_dfs)

            print("Training on {} attacks, {} moves and {} casts".format(len(training_attack_df.index), len(training_move_df.index), len(training_cast_df.index)))
            print("Testing on {} attacks, {} moves and {} casts".format(len(testing_attack_df.index), len(testing_move_df.index), len(testing_cast_df.index)))

            for model_type, train_df, test_df in [("ATTACK", training_attack_df, testing_attack_df),
                                                  ("MOVE", training_move_df, testing_move_df),
                                                  ("CAST", training_cast_df, testing_cast_df)]:


                self._fit_model(model_type, train_df)
                acc, pre, rec = self._test_model(model_type, test_df)
                data.append("{},{},cross_validate{},{},{},{}\n"
                            .format(model_type, type(self.models[model_type]).__name__, cv_splits,
                                    acc, pre, rec))

                print("{}: {},{},{}".format(model_type, acc, pre, rec))

        return data
        

    def _fit_model(self, model_type, df):
        y = df["steamid"].map(lambda steamid: is_filter_player(steamid, self.filter_id))
        X = df.drop("steamid", 1)

        self.models[model_type].fit(X, y)


    def _test_model(self, model_type, test_df):
        y = test_df["steamid"].map(lambda steamid: is_filter_player(steamid, self.filter_id))
        X = test_df.drop("steamid", 1)

        predictions = self.models[model_type].predict(X)

        accuracy = accuracy_score(y, predictions)
        precision = precision_score(y, predictions)
        recall = recall_score(y, predictions)

        return accuracy, precision, recall

