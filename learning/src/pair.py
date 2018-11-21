import math
import pandas as pd

from items import get_hashed_items_df, get_onehot_items_df


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
    dfs0 = [get_properties(df, 0, splits) for df in get_action_dfs(file0)]
    dfs1 = [get_properties(df, 1, splits) for df in get_action_dfs(file1)]
    
    dfs = []
    for i in range(len(dfs0)):
        df0 = dfs0[i]
        df1 = dfs1[i]
        
        df0["tmp"] = 1
        df1["tmp"] = 1

        dfs.append(pd.merge(df0, df1, how="inner").drop("tmp", 1))
    
    return dfs
    
    
def get_properties(raw_df, fid, splits):
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
        "{}-{}-{}-split{}".format(stats.columns[col],stats.index[row], fid, i) 
        for row in range(len(stats.index)) 
        for col in range(len(stats.columns.values))
    ]


def get_data(stats):
    return [
        stats.iloc[row,col]
        for row in range(len(stats.index))
        for col in range(len(stats.columns.values))
    ]


def get_mouse_dfs(pair, splits, separate=False):
    attacks, moves, casts = [], [], []
    file0, file1 = pair

    dfs = get_df(file0, file1, splits)
    return dfs[0], dfs[1], dfs[2]


def get_stats_df(filename):
    filename = filename.replace("mouseaction", "playerstats")
    df = pd.read_csv(filename)

    return df

def concat_preprocess(df, i):
    df.columns = ["{}-{}".format(colname, i) for colname in df.columns]
    df["tmp"] = 1

    return df


def get_pair_dfs(pair, func):
    file0, file1 = pair
    df0 = concat_preprocess(func(file0), 0)
    df1 = concat_preprocess(func(file1), 1)

    return pd.merge(df0, df1, how="inner").drop("tmp", 1)


def get_items_df(pair, period):
    file0, file1 = pair
    df0 = concat_preprocess(get_onehot_items_df(file0, period), 0)
    df1 = concat_preprocess(get_onehot_items_df(file1, period), 1)

    return pd.merge(df0, df1, how="inner").drop("tmp", 1)


class Pair:
    def __init__(self, pair, y, splits):
        attack_df, move_df, cast_df = get_mouse_dfs(pair, splits)
        stats_df = get_pair_dfs(pair, get_stats_df)
        start_items = get_items_df(pair, "START_GAME")
        end_items = get_items_df(pair, "END_GAME")

        self.files = pair
        self.splits = splits

        self.attack_df = attack_df
        self.move_df = move_df
        self.cast_df = cast_df
        self.stats_df = stats_df
        self.start_items_df = start_items
        self.end_items_df = end_items

        self.y = y


    def get_df(self, df_type, split_num):
        if df_type == "ATTACK":
            return self._get_split_df(self.attack_df, split_num)
        elif df_type == "MOVE":
            return self._get_split_df(self.move_df, split_num)
        elif df_type == "CAST":
            return self._get_split_df(self.cast_df, split_num)
        elif df_type == "STATS":
            return self.stats_df
        elif df_type == "START_ITEMS":
            return self.start_items_df
        elif df_type == "END_ITEMS":
            return self.end_items_df


    def _get_split_df(self, df, split_num):
        if split_num > 0:
            return df.filter(regex="-split{}".format(split_num - 1))
        else:
            return df
