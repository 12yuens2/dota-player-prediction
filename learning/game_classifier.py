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
    
def get_dfs(csvpath, dropna=True):
    raw_df = pd.read_csv(csvpath)
    if dropna:
        raw_df = raw_df.dropna()
    
    attack_df = get_action_df(raw_df, "ATTACK")
    move_df = get_action_df(raw_df, "MOVE")
    cast_df = get_action_df(raw_df, "CAST")

    return attack_df, move_df, cast_df



class Game:

    def __init__(self, csv_file):
        a, m, c = get_dfs(csv_file)
        
        self.attack_df = a
        self.move_df = m
        self.cast_df = c
        
        self.csv_file = csv_file


class GameClassifier:

    def __init__(self, filter_id, attack_model, move_model, cast_model):
        self.filter_id = filter_id
        self.attack_model = attack_model
        self.move_model = move_model
        self.cast_model = cast_model


    def cross_validate(self, games, ys, splits=3):
        skf = StratifiedKFold(n_splits=splits)

        for train, test in skf.split(games, ys):
            training_games = [games[i] for i in train]
            testing_games = [games[i] for i in test]
            y_test = [ys[i] for i in test]

            
            for model,train_df in self.concat_data(training_games):
                self.fit(model, train_df)

            predictions = self.predict(testing_games)
            print("Predictions: {}".format(predictions))
            print("Actual:      {}".format(y_test))
            print("KFold accuracy: {}, precision: {}, recall: {}\n".format(accuracy_score(y_test, predictions),
                                                                          precision_score(y_test, predictions),
                                                                          recall_score(y_test, predictions)))


    def concat_data(self, training_games):
        attack_df, move_df, cast_df = self.concat_games(training_games)

        return ((self.attack_model,attack_df),
                (self.move_model,move_df),
                (self.cast_model,cast_df))


    def concat_games(self, games):
        attack_df = pd.concat([game.attack_df for game in games])
        move_df = pd.concat([game.move_df for game in games])
        cast_df = pd.concat([game.cast_df for game in games])
        
        return attack_df, move_df, cast_df


    def get_y(self, df):
        return df["steamid"].map(lambda steamid: is_filter_player(steamid, self.filter_id))

    
    def fit(self, model, df):
        y = self.get_y(df)
        X = df.drop("steamid", 1)
        model.fit(X, y)

    
    def get_percents(self, model, df):
        predictions = model.predict(df.drop("steamid", 1))
        return sum(predictions)/len(predictions)

    
    def get_probas(self, model, df):
        probabilities = model.predict_proba(df.drop("steamid", 1))
        return sum(probabilities)/len(probabilities)

    
    def predict(self, games):
        predictions = []
        for game in games:
            attack_score = self.get_percents(self.attack_model, game.attack_df)
            move_score = self.get_percents(self.move_model, game.move_df)
            cast_score = self.get_percents(self.cast_model, game.cast_df)
            
            attack_proba = self.get_probas(self.attack_model, game.attack_df)
            move_proba = self.get_probas(self.move_model, game.move_df)
            cast_proba = self.get_probas(self.cast_model, game.cast_df)
            
            total_len = len(game.attack_df.index) + len(game.move_df.index) + len(game.cast_df.index)
            attack_weight = len(game.attack_df.index)/total_len
            move_weight = len(game.move_df.index)/total_len
            cast_weight = len(game.cast_df.index)/total_len

            if attack_score + move_score + cast_score > 1.5:
                predictions.append(1)
            else:
                predictions.append(0)

        return predictions
