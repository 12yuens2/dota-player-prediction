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
lr_stats_items_hashed = { **lr_stats_map, **lr_items_hashed_map }
lr_stats_items_onehot = { **lr_stats_map, **lr_items_onehot_map }
lr_stats_items_start = { **lr_stats_map, **lr_items_start_map }
lr_stats_items_select = { **lr_stats_map, **lr_items_select_map }
lr_move_stats_items_hashed = { **lr_move_map, **lr_stats_map, **lr_items_hashed_map }
lr_move_stats_items_onehot = { **lr_move_map, **lr_stats_map, **lr_items_onehot_map }
lr_move_stats_items_start = { **lr_move_map, **lr_stats_map, **lr_items_start_map }
lr_move_stats_items_select = { **lr_move_map, **lr_stats_map, **lr_items_select_map }



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
rf_stats_items_hashed = { **rf_stats_map, **rf_items_hashed_map }
rf_stats_items_onehot = { **rf_stats_map, **rf_items_onehot_map }
rf_stats_items_start = { **rf_stats_map, **rf_items_start_map }
rf_stats_items_select = { **rf_stats_map, **rf_items_select_map }
rf_move_stats_items_hashed = { **rf_move_map, **rf_stats_map, **rf_items_hashed_map }
rf_move_stats_items_onehot = { **rf_move_map, **rf_stats_map, **rf_items_onehot_map }
rf_move_stats_items_start = { **rf_move_map, **rf_stats_map, **rf_items_start_map }
rf_move_stats_items_select = { **rf_move_map, **rf_stats_map, **rf_items_select_map }


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
mlp_stats_items_hashed = { **mlp_stats_map, **mlp_items_hashed_map }
mlp_stats_items_onehot = { **mlp_stats_map, **mlp_items_onehot_map }
mlp_stats_items_start = { **mlp_stats_map, **mlp_items_start_map }
mlp_stats_items_select = { **mlp_stats_map, **mlp_items_select_map }
mlp_move_stats_items_hashed = { **mlp_move_map, **mlp_stats_map, **mlp_items_hashed_map }
mlp_move_stats_items_onehot = { **mlp_move_map, **mlp_stats_map, **mlp_items_onehot_map }
mlp_move_stats_items_start = { **mlp_move_map, **mlp_stats_map, **mlp_items_start_map }
mlp_move_stats_items_select = { **mlp_move_map, **mlp_stats_map, **mlp_items_select_map }


models = [
    ("Logistic Regression", lr_move_map, "mouse"),
    ("Logistic Regression", lr_stats_map, "stats"),
    ("Logistic Regression", lr_items_hashed_map, "items-hashed"),
    ("Logistic Regression", lr_items_onehot_map, "items-onehot"),
    ("Logistic Regression", lr_items_start_map, "items-starting"),
    ("Logistic Regression", lr_items_select_map, "items-select"),

    ("Logistic Regression", lr_move_stats, "mouse-stats"),
    ("Logistic Regression", lr_move_items_hashed, "mouse-items-hashed"),
    ("Logistic Regression", lr_move_items_onehot, "mouse-items-onehot"),
    ("Logistic Regression", lr_move_items_start, "mouse-items-start"),
    ("Logistic Regression", lr_move_items_select, "mouse-items-select"),
    ("Logistic Regression", lr_stats_items_hashed, "stats-items-hashed"),
    ("Logistic Regression", lr_stats_items_onehot, "stats-items-onehot"),
    ("Logistic Regression", lr_stats_items_start, "stats-items-start"),
    ("Logistic Regression", lr_stats_items_select, "stats-items_select"),
    ("Logistic Regression", lr_move_stats_items_hashed, "mouse-stats-items-hashed"),
    ("Logistic Regression", lr_move_stats_items_onehot, "mouse-stats-items-onehot"),
    ("Logistic Regression", lr_move_stats_items_start, "mouse-stats-items-start"),
    ("Logistic Regression", lr_move_stats_items_select, "mouse-stats-items-select"),


    ("Random Forest", rf_move_map, "mouse"),
    ("Random Forest", rf_stats_map, "stats"),
    ("Random Forest", rf_items_hashed_map, "items-hashed"),
    ("Random Forest", rf_items_onehot_map, "items-onehot"),
    ("Random Forest", rf_items_start_map, "items-starting"),
    ("Random Forest", rf_items_select_map, "items-select"),

    ("Random Forest", rf_move_stats, "mouse-stats"),
    ("Random Forest", rf_move_items_hashed, "mouse-items-hashed"),
    ("Random Forest", rf_move_items_onehot, "mouse-items-onehot"),
    ("Random Forest", rf_move_items_start, "mouse-items-start"),
    ("Random Forest", rf_move_items_select, "mouse-items-select"),
    ("Random Forest", rf_stats_items_hashed, "stats-items-hashed"),
    ("Random Forest", rf_stats_items_onehot, "stats-items-onehot"),
    ("Random Forest", rf_stats_items_start, "stats-items-start"),
    ("Random Forest", rf_stats_items_select, "stats-items_select"),
    ("Random Forest", rf_move_stats_items_hashed, "mouse-stats-items-hashed"),
    ("Random Forest", rf_move_stats_items_onehot, "mouse-stats-items-onehot"),
    ("Random Forest", rf_move_stats_items_start, "mouse-stats-items-start"),
    ("Random Forest", rf_move_stats_items_select, "mouse-stats-items-select"),


    ("Multi-layer classifier", mlp_move_map, "mouse"),
    ("Multi-layer classifier", mlp_stats_map, "stats"),
    ("Multi-layer classifier", mlp_items_hashed_map, "items-hashed"),
    ("Multi-layer classifier", mlp_items_onehot_map, "items-onehot"),
    ("Multi-layer classifier", mlp_items_start_map, "items-starting"),
    ("Multi-layer classifier", mlp_items_select_map, "items-select"),

    ("Multi-layer classifier", mlp_move_stats, "mouse-stats"),
    ("Multi-layer classifier", mlp_move_items_hashed, "mouse-items-hashed"),
    ("Multi-layer classifier", mlp_move_items_onehot, "mouse-items-onehot"),
    ("Multi-layer classifier", mlp_move_items_start, "mouse-items-start"),
    ("Multi-layer classifier", mlp_move_items_select, "mouse-items-select"),
    ("Multi-layer classifier", mlp_stats_items_hashed, "stats-items-hashed"),
    ("Multi-layer classifier", mlp_stats_items_onehot, "stats-items-onehot"),
    ("Multi-layer classifier", mlp_stats_items_start, "stats-items-start"),
    ("Multi-layer classifier", mlp_stats_items_select, "stats-items_select"),
    ("Multi-layer classifier", mlp_move_stats_items_hashed, "mouse-stats-items-hashed"),
    ("Multi-layer classifier", mlp_move_stats_items_onehot, "mouse-stats-items-onehot"),
    ("Multi-layer classifier", mlp_move_stats_items_start, "mouse-stats-items-start"),
    ("Multi-layer classifier", mlp_move_stats_items_select, "mouse-stats-items-select")
]
