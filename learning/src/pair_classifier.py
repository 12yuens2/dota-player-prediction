import pandas as pd

from sklearn.neural_network import MLPClassifier
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score

from pair import Pair

def print_scores(accuracy, precision, recall):
    print("Accuracy: {}, Precision: {}, Recall: {}".format(accuracy, precision, recall)) 


class PairClassifier:
    
    def __init__(self, attack_model, move_model, cast_model, network_size):
        self.attack_model = attack_model
        self.move_model = move_model
        self.cast_model = cast_model
        
        self.network = MLPClassifier(solver="lbfgs", hidden_layer_sizes=network_size, random_state=42)
        
    def get_pairs_dfs(self, pairs, split_num=-1):
        attacks, moves, casts = [], [], []
        for pair in pairs:
            attacks.append(pair.get_attack_df(split_num))
            moves.append(pair.get_move_df(split_num))
            casts.append(pair.get_cast_df(split_num))
            
        return (self.attack_model, pd.concat(attacks)), (self.move_model, pd.concat(moves)), (self.cast_model, pd.concat(casts))
        
    def train(self, pairs, y, split_num=-1):
        for model, train_df in self.get_pairs_dfs(pairs, split_num):
            model.fit(train_df, y)
        self.fit_network(pairs, y, split_num)
        
    def fit_network(self, pairs, y, split_num):
        X = [self.get_all_probas(pair, split_num) for pair in pairs]
        self.network.fit(X, y)
        
    def get_proba(self, model, df):
        return model.predict_proba(df)[0]
        
    def get_all_probas(self, pair, split_num=-1):
        attack_proba = self.get_proba(self.attack_model, pair.get_attack_df(split_num))
        move_proba = self.get_proba(self.move_model, pair.get_move_df(split_num))
        cast_proba = self.get_proba(self.cast_model, pair.get_cast_df(split_num))
        
        return [attack_proba[1], move_proba[1], cast_proba[1]]
        
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
        
