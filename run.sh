HERO_ID=$1
NUM_PLAYERS=$2
NUM_GAMES=$3
SAVE_PATH=/cs/scratch/sy35/dota-data/$HERO_ID
REPLAY_PATH=$SAVE_PATH/replays

mkdir -p $REPLAY_PATH
node download.js $HERO_ID $NUM_PLAYERS $NUM_GAMES
find $REPLAY_PATH -name "*.bz2" -size -2k -delete
