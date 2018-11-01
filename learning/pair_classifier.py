import math
import pandas as pd

from sklearn.neural_network import MLPClassifier
from sklearn.metrics import accuracy_score
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score


def get_action_df(raw_df, action):
    return raw_df.loc[raw_df["actionType"] == action].drop("actionType", 1)

def get_action_dfs(csvpath, dropna=True):
    raw_df = pd.read_csv(csvpath)
    if dropna:
        raw_df = raw_df.dropna()
    
    attack_df = get_action_df(raw_df, "ATTACK")
    move_df = get_action_df(raw_df, "MOVE")
    cast_df = get_action_df(raw_df, "CAST")

    return [attack_df, move_df, cast_df]

def get_df(file0, file1, splits):
    dfs0 = [get_stats(df, 0, splits) for df in get_action_dfs(file0)]
    dfs1 = [get_stats(df, 1, splits) for df in get_action_dfs(file1)]
    
    dfs = []
    for i in range(len(dfs0)):
        df0 = dfs0[i]
        df1 = dfs1[i]
        
        df0["tmp"] = 1
        df1["tmp"] = 1

        dfs.append(pd.merge(df0, df1, how="inner").drop("tmp", 1))
    
    return dfs
    
    
def get_stats(raw_df, fid, splits):
    i = 0
    data = []
    headers = []
    for df in split_df(raw_df, splits):
        stats = df.describe().drop("count", 0).drop("steamid", 1).fillna(0)
        headers.extend(get_headers(stats, fid, i))
        data.extend(get_data(stats))
        i += 1
       
    df = pd.DataFrame(data=[data],columns=headers)
    
    return df


def split_df(df, splits):
    rows = int(math.ceil(len(df.index)/float(splits)))
    dfs = []
    
    while len(df) > rows:
        tmp = df[:rows]
        dfs.append(tmp)
        df = df[rows:]
    else:
        dfs.append(df)

    return dfs    


def get_headers(stats, fid, i):
    return [
        "{}-{}-{}-{}".format(stats.columns[col],stats.index[row], fid, i) 
        for row in range(len(stats.index)) 
        for col in range(len(stats.columns.values))
    ]

def get_data(stats):
    return [
        stats.iloc[row,col]
        for row in range(len(stats.index))
        for col in range(len(stats.columns.values))
    ]

def get_pair_dfs(pair, splits):
        attacks, moves, casts = [], [], []
        file0, file1 = pair
        
        dfs = get_df(file0, file1, splits)
        
        return dfs[0], dfs[1], dfs[2]

def print_scores(accuracy, precision, recall):
    print("Accuracy: {}, Precision: {}, Recall: {}".format(accuracy, precision, recall)) 
    

class Pair:
    def __init__(self, pair, splits):
        attack_df, move_df, cast_df = get_pair_dfs(pair, splits)
        
        self.attack_df = attack_df
        self.move_df = move_df
        self.cast_df = cast_df
        

class PairClassifier:
    
    def __init__(self, attack_model, move_model, cast_model, network_size):
        self.attack_model = attack_model
        self.move_model = move_model
        self.cast_model = cast_model
        
        self.network = MLPClassifier(solver="lbfgs", hidden_layer_sizes=network_size, random_state=42)
        
    def get_pairs_dfs(self, pairs):
        attacks, moves, casts = [], [], []
        for pair in pairs:
            attacks.append(pair.attack_df)
            moves.append(pair.move_df)
            casts.append(pair.cast_df)
            
        return (self.attack_model, pd.concat(attacks)), (self.move_model, pd.concat(moves)), (self.cast_model, pd.concat(casts))
        
    def train(self, pairs, y):
        for model, train_df in self.get_pairs_dfs(pairs):
            model.fit(train_df, y)
        self.fit_network(pairs, y)
        
    def fit_network(self, pairs, y):
        X = [self.get_all_probas(pair) for pair in pairs]
        self.network.fit(X, y)
        
    def get_proba(self, model, df):
        return model.predict_proba(df)[0]
        
    def get_all_probas(self, pair):
        attack_proba = self.get_proba(self.attack_model, pair.attack_df)
        move_proba = self.get_proba(self.move_model, pair.move_df)
        cast_proba = self.get_proba(self.cast_model, pair.cast_df)
        
        return [attack_proba[1], move_proba[1], cast_proba[1]]
        
    def predict(self, pairs):
        probabilities = [self.get_all_probas(pair) for pair in pairs]
        return self.network.predict(probabilities)
    
    def test(self, pairs, y):
        predictions = self.predict(pairs)
        
        accuracy = accuracy_score(y, predictions)
        precision = precision_score(y, predictions)
        recall = recall_score(y, predictions)

        print_scores(accuracy, precision, recall)

        return accuracy, precision, recall
        