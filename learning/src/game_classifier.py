import pandas as pd

from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.neural_network import MLPClassifier


def is_filter_player(steam_id, filter_id):
    if steam_id == filter_id:
        return 1
    else:
        return 0


class GameClassifier:

    def __init__(self, filter_id, model_map, network_size):
        self.filter_id = filter_id
        self.model_map = model_map

        self.network = MLPClassifier(solver='lbfgs', hidden_layer_sizes=network_size, random_state=42)


    def cross_validate(self, games, splits=3):
        ys = [self.contains_player(game) for game in games]
        skf = StratifiedKFold(n_splits=splits)

        data = []
        for train, test in skf.split(games, ys):
            training_games = [games[i] for i in train]
            testing_games = [games[i] for i in test]
            y_train = [ys[i] for i in train]
            y_test = [ys[i] for i in test]

            print("Training {} pos, {} neg -- Testing {} pos, {} neg"
                  .format(y_train.count(1), y_train.count(0), y_test.count(1), y_test.count(0)))
            
            for feature,train_df in self.concat_games(training_games).items():
                model = self.model_map[feature]
                self.fit(model, train_df)
            self.fit_network(training_games, y_train)

            acc, pre, rec = self.test_model(y_test, testing_games)
            features = ["{}-{}".format(feature, type(model).__name__) for feature,model in self.model_map.items()]
            print("{}, {}, {}".format(acc, pre, rec))
            data.append("'{}',cross_validate{},{},{},{}\n"
                        .format(",".join(features), splits, acc, pre, rec))

        return data


    def contains_player(self, game):
        if str(self.filter_id) in game.csv_file:
            return 1
        else:
            return 0


    def concat_games(self, games):
        dfs = {}
        for feature,model in self.model_map.items():
            df = pd.concat([game.get_df(feature) for game in games])
            dfs[feature] = df

        return dfs


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
        X = df.drop("steamid", 1)

        predictions = model.predict(X)
        return sum(predictions)/len(predictions)

    
    def get_all_probas(self, game):
        probas = []
        for feature, model in self.model_map.items():
            probas.append(self.get_probas(model, game.get_df(feature)))
        
        return [proba[1] for proba in probas]


    def get_probas(self, model, df):
        X = df.drop("steamid", 1)
        probabilities = model.predict_proba(X)
        return sum(probabilities)/len(probabilities)

    
    #def vote(self, *probabilities):
    #    yes = 0
    #    no = 0
    #    for p in probabilities:
    #        no_prob = p[0]
    #        yes_prob = p[1]
    #        
    #        if yes_prob > no_prob:
    #            yes += 1
    #        else:
    #            no += 1
    #    
    #    return (yes, no)
    #
    #
    #def predict_voting(self, games):
    #    predictions = []
    #    for game in games:           
    #        attack_proba = self.get_probas(self.attack_model, game.attack_df)
    #        move_proba = self.get_probas(self.move_model, game.move_df)
    #        cast_proba = self.get_probas(self.cast_model, game.cast_df)

    #        yes_votes, no_votes = self.vote(attack_proba, move_proba, cast_proba)

    #        if yes_votes > no_votes:
    #            predictions.append(1)
    #        else:
    #            predictions.append(0)

    #    return predictions

    def test_model(self, y, games):
        predictions = self.predict_network(games)

        accuracy = accuracy_score(y, predictions)
        precision = precision_score(y, predictions)
        recall = recall_score(y, predictions)

        return accuracy, precision, recall

    

    def predict_network(self, games):
        predictions = self.network.predict([self.get_all_probas(game) for game in games])
        return predictions
