HERO_ID=$1
NUM_PLAYERS=$2
NUM_GAMES=$3
SAVE_PATH=/cs/scratch/sy35/dota-data/$HERO_ID
REPLAY_PATH=$SAVE_PATH/replays
DATA_PATH=$SAVE_PATH/data

#if [ $# -lt 3 ] 
#then
#    echo "Not enough args"
#    exit 1
#fi


# Download replays
echo "Downloading replays"
mkdir -p $REPLAY_PATH
node replay-download/download.js $HERO_ID $NUM_PLAYERS $NUM_GAMES
find $REPLAY_PATH -name "*.bz2" -size -2k -delete

# Parse replays
echo "Parsing replays"
mkdir -p $DATA_PATH
java -jar dota-parser-master/target/dota-parser-1.0.jar $REPLAY_PATH
find $DATA_PATH -name "*.csv" -size -10k -delete

