import pandas as pd
import os

from sklearn.feature_extraction import FeatureHasher

def hash_transform(df, feature, n_features):
    h = FeatureHasher(n_features=n_features, input_type="string")

    hashed = pd.DataFrame(
        data = h.transform(df[feature]).toarray(),
        columns = ["{}_{}".format(feature, i) for i in range(n_features)]
    )

    # Replace feature with hashed feature
    return df.join(hashed).drop(feature, 1)


def get_hashed_items_df(filename, period):
    filename = filename.replace("mouseaction", "iteminfo")

    items_df = pd.read_csv(filename)

    features = ["inv_{}".format(i) for i in range(6)]
    features.extend(["backpack_{}".format(i) for i in range(3)])
    features.extend(["stash_{}".format(i) for i in range(8)])

    for feature in features:
        items_df = hash_transform(items_df, feature, 6)

    return items_df[items_df["period"] == period].drop("period", 1)
