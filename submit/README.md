# Code and usage

The code is split into 3 parts: `replay-download`, `dota-parser` and `machine-learning`. A script `run.sh` can be used to download and parse the downloaded replays all at once. Please note that no raw data is provided due to the size of the replays and binary objects. New data can be easily downloaded using the download script to be parsed and trained on following the steps below. Also note that since the process contains several different parts, running the full process from getting the replays to machine learning can take several hours, even for a small amount of data. 

## `replay-download`

This directory contains the Javascript code for downloading replay files from OpenDota and Valve's servers. It takes 4 arguments:
```
node download.js [hero_id] [num_players] [num_games] [download_path]
```
- `hero_id` is the id of the chosen hero for all replays
- `num_players` is the number of players to download replays from
- `num_games` is the number of replays _per player_ to download
- `download_path` is the directory where the replays are downloaded to

Replays downloaded which are small in size are likely to be errors from the server. Increase the number of players and games to find replays if downloads mostly results in errors.


## `dota-parser`

This directory contains the Java code for parsing data from replays. Build the project with
```
mvn package
```

To run the parser, use
```
java -jar target/dota-parser-1.0.jar [replay_path]
```
The `replay_path` is the location where all the replays are stored. The data is then written in csv format to `../../data` of the replay path. Please refer to `run.sh` for the various subdirectories that should be created for the various csv outputs.


## `machine-learning`

This directory contains the Python code for preprocessing and machine learning. Install dependencies with
```
pip install -r requirements.txt
```

There are several different experiments that can be run with
```
python run_[classifier].py ...
```

- `run_move_classifier.py [data_path] [cv] [filter_id]` - This runs an experiment for each individual mouse movement.
    - `data_path` is the location where the parser output is stored. As the data may be split into many subdirectories, use the `mouseaction` directory
    - `cv` is the number of k-fold cross validation to perform.
    - `filter_id` is the id of the positive sample player

- `run_game_classifier.py [data_path] [cv] [filter_id]` - This runs an experiment for each match. The parameters are the same as the move classifier
- `run_pair_classifier.py [data_path] [cv] [output_path]` - This runs an experiment for pairs of matches. To create the pairs, run `create_featureset.py`.


# Data

The data provided in `data` are the results of the machine learning experiments. Notably, `14-game-rf.csv` and `15-game-rf.csv` were specialised experiments to show more detailed data while the other three are the general experiments described in the report.

