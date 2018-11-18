import pandas as pd

from sklearn.neural_network import MLPClassifier
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from pair import Pair

def print_scores(accuracy, precision, recall):
    print("Accuracy: {}, Precision: {}, Recall: {}".format(accuracy, precision, recall)) 


class PairClassifier:
    
    def __init__(self, model_map, network_size):
        self.model_map = model_map
        self.network = MLPClassifier(solver="lbfgs", hidden_layer_sizes=network_size, random_state=42)
        
    def concat_pairs(self, pairs, split_num=-1):
        dfs = {}
        for feature, model in self.model_map.items():
            df = pd.concat([pair.get_df(feature, split_num) for pair in pairs])
            dfs[feature] = df

        return dfs


    def train(self, pairs, y, split_num=-1):
        for feature, df in self.concat_pairs(pairs, split_num).items():
            self.model_map[feature].fit(df, y)
        self.fit_network(pairs, y, split_num)


    def fit_network(self, pairs, y, split_num):
        X = [self.get_all_probas(pair, split_num) for pair in pairs]
        self.network.fit(X, y)

        
    def get_proba(self, model, df):
        return model.predict_proba(df)[0]


    def get_all_probas(self, pair, split_num=-1):
        probas = []
        for feature, model in self.model_map.items():
            probas.append(self.get_proba(model, pair.get_df(feature, split_num)))

        return [proba[1] for proba in probas]


    def predict(self, pairs, split_num=-1):
        probabilities = [self.get_all_probas(pair, split_num) for pair in pairs]
        return self.network.predict(probabilities)


    def test(self, pairs, y, split_num=-1):
        predictions = self.predict(pairs, split_num)
        
        accuracy = accuracy_score(y, predictions)
        precision = precision_score(y, predictions)
        recall = recall_score(y, predictions)

        print_scores(accuracy, precision, recall)

        return accuracy, precision, recall
        
