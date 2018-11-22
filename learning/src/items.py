import pandas as pd
import os

from sklearn.feature_extraction import FeatureHasher

def _hash_transform(df, feature, n_features):
    h = FeatureHasher(n_features=n_features, input_type="string")

    hashed = pd.DataFrame(
        data = h.transform(df[feature]).toarray(),
        columns = ["{}_{}".format(feature, i) for i in range(n_features)]
    )

    # Replace feature with hashed feature
    return df.join(hashed).drop(feature, 1)


def _get_slot_features():
    features = ["inv_{}".format(i) for i in range(6)]
    features.extend(["backpack_{}".format(i) for i in range(3)])
    features.extend(["stash_{}".format(i) for i in range(8)])

    return features


def _get_item_names():
    return open("../items.txt", "r").read().splitlines()


def _get_onehot_encoded_data(df, features):
    encoded = []

    for i, row in df.iterrows():
        data = {"steamid": row["steamid"]}
        for slot in features:
            item = row[slot]
            data["{}_{}".format(slot, item)] = 1

        encoded.append(data)

    return encoded

    
# Public functions for others to import

def get_hashed_items_df(filename, period):
    filename = filename.replace("mouseaction", "iteminfo")
    items_df = pd.read_csv(filename)

    features = _get_slot_features()
    for feature in features:
        items_df = _hash_transform(items_df, feature, 6)

    return items_df[items_df["period"] == period].drop("period", 1)


def get_onehot_items_df(filename, period):
    filename = filename.replace("mouseaction", "iteminfo")
    items_df = pd.read_csv(filename)
    items_df = items_df[items_df["period"] == period].drop("period", 1)

    features = _get_slot_features()
    item_names = _get_item_names()
    item_slots = ["{}_{}".format(slot, item) for slot in features for item in item_names]
    item_slots.append("steamid")

    encoded_data = _get_onehot_encoded_data(items_df, features)
    encoded_df = pd.DataFrame(encoded_data, columns=item_slots).fillna(0)

    return encoded_df

    
