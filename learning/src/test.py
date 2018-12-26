import pickle
import sys

f = open("/cs/scratch/sy35/dota-data/35/dfs/train/pairs1.df", "rb")
pairs = pickle.load(f)

print(pairs[0].get_df("END_ITEMS_HASH", -1).columns.values)
print(pairs[0].get_df("START_ITEMS_ONLY", -1).columns.values)
