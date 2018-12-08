from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier

def lr(solver, alpha, hidden_layer_sizes, class_weight):
    return LogisticRegression(class_weight=class_weight)

def rf(solver, alpha, hidden_layer_sizes, class_weight):
    return RandomForestClassifier(class_weight=class_weight)

def mlp(solver, alpha, hidden_layer_sizes, class_weight):
    return MLPClassifier(solver=solver, alpha=alpha, hidden_layer_sizes=hidden_layer_sizes)


def move_map(classifier, solver="lbfgs", alpha=0.001, hidden_layer_sizes=(100,), class_weight="balanced"):
    return  {
        "ATTACK": classifier(solver, alpha, hidden_layer_sizes, class_weight),
        "MOVE"  : classifier(solver, alpha, hidden_layer_sizes, class_weight),
        "CAST"  : classifier(solver, alpha, hidden_layer_sizes, class_weight)
    }

def stats_map(classifier, solver="lbfgs", alpha=0.001, hidden_layer_sizes=(100,), class_weight="balanced"):
    return { "STATS": classifier(solver, alpha, hidden_layer_sizes, class_weight) }

def items_hashed(classifier, solver="lbfgs", alpha=0.001, hidden_layer_sizes=(100,), class_weight="balanced"):
    return {
        "START_ITEMS_HASH": classifier(solver, alpha, hidden_layer_sizes, class_weight),
        "END_ITEMS_HASH"  : classifier(solver, alpha, hidden_layer_sizes, class_weight)
    }

def items_onehot(classifier, solver="lbfgs", alpha=0.001, hidden_layer_sizes=(100,), class_weight="balanced"):
    return {
        "START_ITEMS_ONEHOT": classifier(solver, alpha, hidden_layer_sizes, class_weight),
        "END_ITEMS_ONEHOT"   : classifier(solver, alpha, hidden_layer_sizes, class_weight)
    }

def items_start_only(classifier, solver="lbfgs", alpha=0.001, hidden_layer_sizes=(100,), class_weight="balanced"):
    return { "START_ITEMS_ONLY": classifier(solver, alpha, hidden_layer_sizes, class_weight) }

def items_select(classifier, solver="lbfgs", alpha=0.001, hidden_layer_sizes=(100,), class_weight="balanced"):
    return { "SELECT_ITEMS": classifier(solver, alpha, hidden_layer_sizes, class_weight) }

def items_difference(classifier, solver="lbfgs", alpha=0.001, hidden_layer_sizes=(100,), class_weight="balanced"):
    return { "ITEMS_DIFFERENCE": classifier(solver, alpha, hidden_layer_sizes, class_weight) }


lr_move_map = move_map(lr)
lr_stats_map = stats_map(lr)
lr_items_hashed_map = items_hashed(lr)
lr_items_onehot_map = items_onehot(lr)
lr_items_start_map = items_start_only(lr)
lr_items_select_map = items_select(lr)
lr_items_difference = items_difference(lr) # pair only

lr_move_stats = { **lr_move_map, **lr_stats_map }
lr_move_items_hashed = { **lr_move_map, **lr_items_hashed_map }
lr_move_items_onehot = { **lr_move_map, **lr_items_onehot_map }
lr_move_items_start = { **lr_move_map, **lr_items_start_map }
lr_move_items_select = { **lr_move_map, **lr_items_select_map }
lr_move_items_diff  = { **lr_move_map, **lr_items_difference } # pair only
lr_stats_items_hashed = { **lr_stats_map, **lr_items_hashed_map }
lr_stats_items_onehot = { **lr_stats_map, **lr_items_onehot_map }
lr_stats_items_start = { **lr_stats_map, **lr_items_start_map }
lr_stats_items_select = { **lr_stats_map, **lr_items_select_map }
lr_stats_items_diff = { **lr_stats_map, **lr_items_difference } # pair only
lr_move_stats_items_hashed = { **lr_move_map, **lr_stats_map, **lr_items_hashed_map }
lr_move_stats_items_onehot = { **lr_move_map, **lr_stats_map, **lr_items_onehot_map }
lr_move_stats_items_start = { **lr_move_map, **lr_stats_map, **lr_items_start_map }
lr_move_stats_items_select = { **lr_move_map, **lr_stats_map, **lr_items_select_map }
lr_move_stats_items_diff = { **lr_move_map, **lr_stats_map, **lr_items_difference } # pair only


rf_move_map = move_map(rf)
rf_stats_map = stats_map(rf)
rf_items_hashed_map = items_hashed(rf)
rf_items_onehot_map = items_onehot(rf)
rf_items_start_map = items_start_only(rf)
rf_items_select_map = items_select(rf)
rf_items_difference = items_difference(rf) # pair only

rf_move_stats = { **rf_move_map, **rf_stats_map }
rf_move_items_hashed = { **rf_move_map, **rf_items_hashed_map }
rf_move_items_onehot = { **rf_move_map, **rf_items_onehot_map }
rf_move_items_start = { **rf_move_map, **rf_items_start_map }
rf_move_items_select = { **rf_move_map, **rf_items_select_map }
rf_move_items_diff  = { **rf_move_map, **rf_items_difference } # pair only
rf_stats_items_hashed = { **rf_stats_map, **rf_items_hashed_map }
rf_stats_items_onehot = { **rf_stats_map, **rf_items_onehot_map }
rf_stats_items_start = { **rf_stats_map, **rf_items_start_map }
rf_stats_items_select = { **rf_stats_map, **rf_items_select_map }
rf_stats_items_diff = { **rf_stats_map, **rf_items_difference } # pair only
rf_move_stats_items_hashed = { **rf_move_map, **rf_stats_map, **rf_items_hashed_map }
rf_move_stats_items_onehot = { **rf_move_map, **rf_stats_map, **rf_items_onehot_map }
rf_move_stats_items_start = { **rf_move_map, **rf_stats_map, **rf_items_start_map }
rf_move_stats_items_select = { **rf_move_map, **rf_stats_map, **rf_items_select_map }
rf_move_stats_items_diff = { **rf_move_map, **rf_stats_map, **rf_items_difference } # pair only


mlp_move_map = move_map(mlp, hidden_layer_sizes=(256,))
mlp_stats_map = stats_map(mlp, hidden_layer_sizes=(10,))
mlp_items_hashed_map = items_hashed(mlp, hidden_layer_sizes=(10,))
mlp_items_onehot_map = items_onehot(mlp)
mlp_items_start_map = items_start_only(mlp)
mlp_items_select_map = items_select(mlp)
mlp_items_difference = items_difference(mlp, hidden_layer_sizes=(3,)) # pair only

mlp_move_stats = { **mlp_move_map, **mlp_stats_map }
mlp_move_items_hashed = { **mlp_move_map, **mlp_items_hashed_map }
mlp_move_items_onehot = { **mlp_move_map, **mlp_items_onehot_map }
mlp_move_items_start = { **mlp_move_map, **mlp_items_start_map }
mlp_move_items_select = { **mlp_move_map, **mlp_items_select_map }
mlp_move_items_diff  = { **mlp_move_map, **mlp_items_difference } # pair only
mlp_stats_items_hashed = { **mlp_stats_map, **mlp_items_hashed_map }
mlp_stats_items_onehot = { **mlp_stats_map, **mlp_items_onehot_map }
mlp_stats_items_start = { **mlp_stats_map, **mlp_items_start_map }
mlp_stats_items_select = { **mlp_stats_map, **mlp_items_select_map }
mlp_stats_items_diff = { **mlp_stats_map, **mlp_items_difference } # pair only
mlp_move_stats_items_hashed = { **mlp_move_map, **mlp_stats_map, **mlp_items_hashed_map }
mlp_move_stats_items_onehot = { **mlp_move_map, **mlp_stats_map, **mlp_items_onehot_map }
mlp_move_stats_items_start = { **mlp_move_map, **mlp_stats_map, **mlp_items_start_map }
mlp_move_stats_items_select = { **mlp_move_map, **mlp_stats_map, **mlp_items_select_map }
mlp_move_stats_items_diff = { **mlp_move_map, **mlp_stats_map, **mlp_items_difference } # pair only


network_size = (3,)

def lr_model(model_map, name, network_size=(3,)):
    return ("Logistic Regression", model_map, name, network_size)

def rf_model(model_map, name, network_size=(3,)):
    return ("Random Forest", model_map, name, network_size)

def mlp_model(model_map, name, network_size=(3,)):
    return ("Multi-layer Perceptron", model_map, name, network_size)


models = [
    lr_model(lr_move_map, "mouse"),
    lr_model(lr_stats_map, "stats", None),
    lr_model(lr_items_hashed_map, "items-hashed", None),
    lr_model(lr_items_onehot_map, "items-onehot", None),
    lr_model(lr_items_start_map, "items-starting", None),
    lr_model(lr_items_select_map, "items-select", None),

    lr_model(lr_move_stats, "mouse-stats"),
    lr_model(lr_move_items_hashed, "mouse-items-hashed"),
    lr_model(lr_move_items_onehot, "mouse-items-onehot"),
    lr_model(lr_move_items_start, "mouse-items-start"),
    lr_model(lr_move_items_select, "mouse-items-select"),
    lr_model(lr_stats_items_hashed, "stats-items-hashed"),
    lr_model(lr_stats_items_onehot, "stats-items-onehot"),
    lr_model(lr_stats_items_start, "stats-items-start"),
    lr_model(lr_stats_items_select, "stats-items_select"),
    lr_model(lr_move_stats_items_hashed, "mouse-stats-items-hashed"),
    lr_model(lr_move_stats_items_onehot, "mouse-stats-items-onehot"),
    lr_model(lr_move_stats_items_start, "mouse-stats-items-start"),
    lr_model(lr_move_stats_items_select, "mouse-stats-items-select"),


    rf_model(rf_move_map, "mouse"),
    rf_model(rf_stats_map, "stats", None),
    rf_model(rf_items_hashed_map, "items-hashed", None),
    rf_model(rf_items_onehot_map, "items-onehot", None),
    rf_model(rf_items_start_map, "items-starting", None),
    rf_model(rf_items_select_map, "items-select", None),

    rf_model(rf_move_stats, "mouse-stats"),
    rf_model(rf_move_items_hashed, "mouse-items-hashed"),
    rf_model(rf_move_items_onehot, "mouse-items-onehot"),
    rf_model(rf_move_items_start, "mouse-items-start"),
    rf_model(rf_move_items_select, "mouse-items-select"),
    rf_model(rf_stats_items_hashed, "stats-items-hashed"),
    rf_model(rf_stats_items_onehot, "stats-items-onehot"),
    rf_model(rf_stats_items_start, "stats-items-start"),
    rf_model(rf_stats_items_select, "stats-items_select"),
    rf_model(rf_move_stats_items_hashed, "mouse-stats-items-hashed"),
    rf_model(rf_move_stats_items_onehot, "mouse-stats-items-onehot"),
    rf_model(rf_move_stats_items_start, "mouse-stats-items-start"),
    rf_model(rf_move_stats_items_select, "mouse-stats-items-select"),


    mlp_model(mlp_move_map, "mouse"),
    mlp_model(mlp_stats_map, "stats", None),
    mlp_model(mlp_items_hashed_map, "items-hashed", None),
    mlp_model(mlp_items_onehot_map, "items-onehot", None),
    mlp_model(mlp_items_start_map, "items-starting", None),
    mlp_model(mlp_items_select_map, "items-select", None),

    mlp_model(mlp_move_stats, "mouse-stats"),
    mlp_model(mlp_move_items_hashed, "mouse-items-hashed"),
    mlp_model(mlp_move_items_onehot, "mouse-items-onehot"),
    mlp_model(mlp_move_items_start, "mouse-items-start"),
    mlp_model(mlp_move_items_select, "mouse-items-select"),
    mlp_model(mlp_stats_items_hashed, "stats-items-hashed"),
    mlp_model(mlp_stats_items_onehot, "stats-items-onehot"),
    mlp_model(mlp_stats_items_start, "stats-items-start"),
    mlp_model(mlp_stats_items_select, "stats-items_select"),
    mlp_model(mlp_move_stats_items_hashed, "mouse-stats-items-hashed"),
    mlp_model(mlp_move_stats_items_onehot, "mouse-stats-items-onehot"),
    mlp_model(mlp_move_stats_items_start, "mouse-stats-items-start"),
    mlp_model(mlp_move_stats_items_select, "mouse-stats-items-select")
]
