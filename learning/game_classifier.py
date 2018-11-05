import pandas as pd

from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.neural_network import MLPClassifier

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

    def __init__(self, filter_id, attack_model, move_model, cast_model, network_size):
        self.filter_id = filter_id
        self.attack_model = attack_model
        self.move_model = move_model
        self.cast_model = cast_model
        
        self.network = MLPClassifier(solver='lbfgs', hidden_layer_sizes=network_size, random_state=42)


    def cross_validate(self, games, ys, splits=3):
        skf = StratifiedKFold(n_splits=splits)

        for train, test in skf.split(games, ys):
            training_games = [games[i] for i in train]
            testing_games = [games[i] for i in test]
            y_train = [ys[i] for i in train]
            y_test = [ys[i] for i in test]

            
            for model,train_df in self.concat_data(training_games):
                self.fit(model, train_df)
            self.fit_network(training_games, y_train)
                
            predictions_voting = self.predict_voting(testing_games)
            predictions_network = self.predict_network(testing_games)
            #print("Predictions voting: {}".format(predictions_voting))
            #print("Predictions network: {}".format(predictions_network))
            #print("Actual:      {}".format(y_test))
            print("KFold accuracy voting: {}, precision: {}, recall: {}".format(accuracy_score(y_test, predictions_voting),
                                                                                precision_score(y_test, predictions_voting),
                                                                                recall_score(y_test, predictions_voting)))
            
            print("KFold accuracy network: {}, precision: {}, recall: {}\n"
                  .format(accuracy_score(y_test, predictions_network),
                          precision_score(y_test, predictions_network),
                          recall_score(y_test, predictions_network)))
                                                                                   


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

    def fit_network(self, games, ys):
        X = [self.get_all_probas(game) for game in games]
        self.network.fit(X, ys)
                         
    def get_percents(self, model, df):
        predictions = model.predict(df.drop("steamid", 1))
        return sum(predictions)/len(predictions)

    
    def get_all_probas(self, game):
        attack_proba = self.get_probas(self.attack_model, game.attack_df)
        move_proba = self.get_probas(self.move_model, game.move_df)
        cast_proba = self.get_probas(self.cast_model, game.cast_df)
        
        return [attack_proba[1], move_proba[1], cast_proba[1]]
    
    def get_probas(self, model, df):
        probabilities = model.predict_proba(df.drop("steamid", 1))
        return sum(probabilities)/len(probabilities)

    
    def vote(self, *probabilities):
        yes = 0
        no = 0
        for p in probabilities:
            no_prob = p[0]
            yes_prob = p[1]
            
            if yes_prob > no_prob:
                yes += 1
            else:
                no += 1
        
        return (yes, no)
    
    
    def predict_voting(self, games):
        predictions = []
        for game in games:           
            attack_proba = self.get_probas(self.attack_model, game.attack_df)
            move_proba = self.get_probas(self.move_model, game.move_df)
            cast_proba = self.get_probas(self.cast_model, game.cast_df)

            yes_votes, no_votes = self.vote(attack_proba, move_proba, cast_proba)

            if yes_votes > no_votes:
                predictions.append(1)
            else:
                predictions.append(0)

        return predictions


    def predict_network(self, games):
        predictions = self.network.predict([self.get_all_probas(game) for game in games])
        return predictions
