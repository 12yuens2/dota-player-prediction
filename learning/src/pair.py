import math
import pandas as pd

import items


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

        # Use tmp to perge the dataframes
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

    return df.drop("steamid", 1)


def concat_preprocess(df, i):
    df.columns = ["{}-{}".format(colname, i) for colname in df.columns]
    df["tmp"] = 1

    return df


def get_pair_dfs(pair, func):
    file0, file1 = pair
    df0 = concat_preprocess(func(file0), 0)
    df1 = concat_preprocess(func(file1), 1)

    return pd.merge(df0, df1, how="inner").drop("tmp", 1)


def get_items_df(pair, period, func):
    file0, file1 = pair
    df0 = concat_preprocess(func(file0, period), 0).drop("steamid-0", 1)
    df1 = concat_preprocess(func(file1, period), 1).drop("steamid-1", 1)

    return pd.merge(df0, df1, how="inner").drop("tmp", 1)


class Pair:
    def __init__(self, pair, y, splits):
        attack_df, move_df, cast_df = get_mouse_dfs(pair, splits)
        stats_df = get_pair_dfs(pair, get_stats_df)

        self.files = pair
        self.splits = splits

        self.attack_df = attack_df
        self.move_df = move_df
        self.cast_df = cast_df
        self.stats_df = stats_df

        self.start_items_hash_df = get_items_df(pair, "START_GAME", items.get_hashed_items)
        self.end_items_hash_df = get_items_df(pair, "END_GAME", items.get_hashed_items)

        self.start_items_onehot_df = get_items_df(pair, "START_GAME", items.get_onehot_all)
        self.end_items_onehot_df = get_items_df(pair, "END_GAME", items.get_onehot_all)

        self.start_items_diff_df = items.get_item_difference(pair, "START_GAME")
        self.end_items_diff_df = items.get_item_difference(pair, "END_GAME")

        self.start_items_only_df = get_items_df(pair, "", items.get_onehot_starting_only)
        self.select_items_df = get_items_df(pair, "", items.get_onehot_select_only)

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

        elif df_type == "START_ITEMS_HASH":
            return self.start_items_hash_df
        elif df_type == "END_ITEMS_HASH":
            return self.end_items_hash_df
        elif df_type == "START_ITEMS_ONEHOT":
            return self.start_items_onehot_df
        elif df_type == "END_ITEMS_ONEHOT":
            return self.end_items_onehot_df
        elif df_type == "START_ITEMS_ONLY":
            return self.start_items_only_df
        elif df_type == "SELECT_ITEMS":
            return self.select_items_df
        elif df_type == "START_ITEMS_DIFF":
            return self.start_items_diff_df
        elif df_type == "END_ITEMS_DIFF":
            return self.end_items_diff_df


    def _get_split_df(self, df, split_num):
        if split_num > 0:
            return df.filter(regex="-split{}".format(split_num - 1))
        else:
            return df
