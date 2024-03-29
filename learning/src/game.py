import pandas as pd

import items


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


def stats_df_from_file(filename):
    filename = filename.replace("mouseaction", "playerstats")
    df = pd.read_csv(filename)

    return df.fillna(0)


class Game:

    def __init__(self, csv_file):
        a, m, c = get_dfs(csv_file)
        
        self.attack_df = a
        self.move_df = m
        self.cast_df = c
        self.stats_df = stats_df_from_file(csv_file)

        self.start_items_hash_df = items.get_hashed_items(csv_file, "START_GAME")
        self.end_items_hash_df = items.get_hashed_items(csv_file, "END_GAME")
        
        self.start_items_onehot_df = items.get_onehot_all(csv_file, "START_GAME")
        self.end_items_onehot_df = items.get_onehot_all(csv_file, "END_GAME")

        self.start_items_only_df = items.get_onehot_starting_only(csv_file, "")
        self.select_items_df = items.get_onehot_select_only(csv_file, "")

        self.csv_file = csv_file


    def get_df(self, df_type, split_num):
        if df_type == "ATTACK":
            return self.attack_df
        elif df_type == "MOVE":
            return self.move_df
        elif df_type == "CAST":
            return self.cast_df

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
