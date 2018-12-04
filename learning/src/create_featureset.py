import pandas as pd

import math
import itertools
import os
import ntpath
import random

from pair import Pair


def get_pair_names(path):
    files = ["{}/{}".format(path, file) for file in os.listdir(path)]
    return list(itertools.permutations(files, 2))

def get_pairs(pair_names, ys, splits):
    pairs = []
    for pair,y in zip(pair_names, ys):
        try:
            p = Pair(pair, y, splits)
            pairs.append(p)
        except FileNotFoundError:
            print("File not found for {}".format(pair))

    return pairs

def get_playerid(name):
    return ntpath.basename(name)[:17]

def is_same_player(id1, id2):
    return 1 if id1 == id2 else 0

def get_ys(pairs):
    return [
        is_same_player(get_playerid(file0), get_playerid(file1))
        for file0,file1 in pairs
    ]

def sample_filter(sample, prob):
    if sample[1] == 0:
        if random.random() < prob:
            return False
        else:
            return True
    else:
        return False

def sample(X, y, ratio):
    prob = float(y.count(1))/float(y.count(0))
    combine = [(X[i], y[i]) for i in range(len(X))]
    sample = [s for s in combine if not sample_filter(s, prob)]
    
    return map(list, zip(*sample))

def count_sample(x):
    negatives = x.count(0)
    positives = x.count(1)
    return "{} negative samples and {} positive samples".format(negatives, positives)


import sys
import pickle

def dump_pickle(filepath, obj):
    print("Dumping at {}".format(filepath))
    pickle.dump(obj, open(filepath, "wb"))
    print("Drumped")


path = sys.argv[1]
dfpath = sys.argv[2]
#dfpath = "/cs/scratch/sy35/dota-data/{}/dfs".format(hero_id)
if not os.path.exists(dfpath):
    os.makedirs(dfpath)

#path = "/cs/scratch/sy35/dota-data/{}/data/mouseaction".format(hero_id)
pairs = get_pair_names(path)
ys = get_ys(pairs)
pair_names, y = sample(pairs, ys, 0.5)

print(count_sample(y))

dump_pickle("{}/pairs1.df".format(dfpath), get_pairs(pair_names, y, 1))
dump_pickle("{}/pairs2.df".format(dfpath), get_pairs(pair_names, y, 2))
dump_pickle("{}/pairs3.df".format(dfpath), get_pairs(pair_names, y, 3))
dump_pickle("{}/pairs5.df".format(dfpath), get_pairs(pair_names, y, 5))


