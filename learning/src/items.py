import pandas as pd
import os

from sklearn.feature_extraction import FeatureHasher


def _hash_transform(df, feature, n_features):
    h = FeatureHasher(n_features=n_features, input_type="string")

    hashed = pd.DataFrame(
        data = h.transform(df[feature]).toarray(),
        columns = ["{}_{}".format(feature, i) for i in range(n_features)]
    )

    df["tmp"] = 1
    hashed["tmp"] = 1
    return pd.merge(hashed, df, how="inner").drop("tmp", 1)


def _get_slot_features():
    return ["inv_{}".format(i) for i in range(6)]


def _get_all_features():
    features = ["inv_{}".format(i) for i in range(6)]
    features.extend(["backpack_{}".format(i) for i in range(3)])
    features.extend(["stash_{}".format(i) for i in range(8)])

    return features


def _get_item_df(filename, period):
    filename = filename.replace("mouseaction", "iteminfo")
    item_df = pd.read_csv(filename)

    return item_df[item_df["period"] == period].drop("period", 1)


def _get_item_names(item_list):
    return open(item_list, "r").read().splitlines()


def _get_onehot_encoded_data(df, features):
    encoded = []

    for i, row in df.iterrows():
        data = {"steamid": row["steamid"]}
        for slot in features:
            item = row[slot]
            data["{}_{}".format(slot, item)] = 1

        encoded.append(data)

    return encoded


def _get_onehot_items_df(filename, period, item_list):
    items_df = _get_item_df(filename, period)

    features = _get_slot_features()
    item_names = _get_item_names(item_list)
    item_slots = ["{}_{}".format(slot, item) for slot in features for item in item_names]
    item_slots.append("steamid")

    encoded_data = _get_onehot_encoded_data(items_df, features)
    encoded_df = pd.DataFrame(encoded_data, columns=item_slots).fillna(0)

    return encoded_df


# Public functions for others to import

def get_hashed_items(filename, period):
    items_df = _get_item_df(filename, period)

    features = _get_slot_features()
    for feature in features:
        items_df = _hash_transform(items_df, feature, 6)

    # Drop old features (backpack and stash)
    for feature in _get_all_features():
        items_df = items_df.drop(feature, 1)

    return items_df


def get_onehot_all(filename, period):
    return _get_onehot_items_df(filename, period, "../items.txt")


def get_onehot_starting_only(filename, period):
    return _get_onehot_items_df(filename, "START_GAME", "../starting_items.txt")


def get_onehot_select_only(filename, period):
    return _get_onehot_items_df(filename, "END_GAME", "../select_items.txt")


def get_item_difference(pair, period):
    items_df1 = _get_item_df(pair[0], period)
    items_df2 = _get_item_df(pair[1], period)
    features = _get_slot_features()

    data = {}
    for slot in features:
        data[slot] = 0
        item1 = items_df1[slot].iloc[0]
        item2 = items_df2[slot].iloc[0]

        if item1 == item2: data[slot] = 1

    return pd.DataFrame([data], columns=features)
