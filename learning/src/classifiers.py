import pandas as pd

from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.neural_network import MLPClassifier


class Classifier:

    def __init__(self, model_map, network_size):
        self.model_map = model_map
        self.network = MLPClassifier(solver='lbfgs', hidden_layer_sizes=network_size, random_state=42)


    def train(self, xs, y, split_num=-1):
        for feature, df in self._concat_xs(xs, split_num).items():
            self._fit(self.model_map[feature], df, y)
        self._fit_network(xs, y, split_num)


    def test(self, xs, y, split_num=-1):
        predictions = self.predict(xs, split_num)

        accuracy = accuracy_score(y, predictions)
        precision = precision_score(y, predictions)
        recall = recall_score(y, predictions)

        print_scores(accuracy, precision, recall)
        return accuracy, precision, recall


    def predict(self, xs, split_num=-1):
        probabilities = [self._get_all_probas(x, split_num) for x in xs]

        return self.network.predict(probabilities)


## Private functions for classifiers ##

    def _concat_xs(self, xs, split_num):
        dfs = {}
        for feature, model in self.model_map.items():

            # Concat all xs into one dataframe
            df = pd.concat([x.get_df(feature, split_num) for x in xs])
            dfs[feature] = df

        return dfs


    def _fit(self, model, df, y):
        model.fit(df, y)

    
    def _fit_network(self, xs, y, split_num):
        xs = [self._get_all_probas(x, split_num) for x in xs]
        self.network.fit(xs, y)


    def _get_all_probas(self, x, split_num):
        probas = []
        for feature, model in self.model_map.items():

            # Get proba[1] for probability of class 1 (positive sample)
            probas.append(self._get_proba(model, x.get_df(feature, split_num))[1])

        return probas


    def _get_proba(self, model, df):
        return model.predict_proba(df)


class GameClassifier(Classifier):

    def __init__(self, filter_id, model_map, network_size):
        super(GameClassifier, self).__init__(model_map, network_size)

        self.filter_id = filter_id


    def contains_player(self, game):
        if str(self.filter_id) in game.csv_file:
            return 1
        else:
            return 0

        
    def _fit(self, model, df, y):
        y = self._get_y(df)
        X = df.drop("steamid", 1)

        model.fit(X, y)


    def _get_y(self, df):
        return df["steamid"].map(lambda steamid: self._is_filter_player(steamid))


    def _is_filter_player(self, steamid):
        return (1 if steamid == self.filter_id else 0)


    def _get_proba(self, model, df):
        X = df.drop("steamid", 1)
        probabilities = model.predict_proba(X)

        # Average the probability of all rows
        return sum(probabilities)/len(probabilities)


class PairClassifier(Classifier):

    def __init__(self, model_map, network_size):
        super(PairClassifier, self).__init__(model_map, network_size)


    def _get_proba(self, model, df):
        return model.predict_proba(df)[0]


## Misc functions
def cross_validate(xs, cv, classifier, get_y, split_num=-1):
    ys = [get_y(x) for x in xs]
    skf = StratifiedKFold(cv)

    for train_index, test_index in skf.split(xs, ys):
        X_train = [xs[i] for i in train_index]
        X_test = [xs[i] for i in test_index]
        y_train = [ys[i] for i in train_index]
        y_test = [ys[i] for i in test_index]

        print("Training {} pos, {} neg -- Testing {} pos, {} neg"
              .format(y_train.count(1), y_train.count(0), y_test.count(1), y_test.count(0)))

        classifier.train(X_train, y_train, split_num)
        classifier.test(X_test, y_test, split_num)


def train_test(X_train, X_test, classifier, get_y, split_num=-1):
    y_train = [get_y(x) for x in train_x]
    y_test = [get_y(x) for x in test_y]

    classifier.train(X_train, y_train, split_num)
    acc, pre, rec = classifier.test(X_test, y_test, split_num)

    # Return csv format of results
    features = ["{}-{}".format(f, type(m).__name__) for f,m in self.model_map.items()]
    return "'{}',{},{},{}".format(",".join(features), acc, pre, rec)


def print_scores(accuracy, precision, recall):
    print("Accuracy: {}, Precision: {}, Recall: {}".format(accuracy, precision, recall))


# TODO voting
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