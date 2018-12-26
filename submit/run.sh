HERO_ID=$1
NUM_PLAYERS=$2
NUM_GAMES=$3
SAVE_PATH=/cs/scratch/sy35/dota-data/$HERO_ID
REPLAY_PATH=$SAVE_PATH/replays
DATA_PATH=$SAVE_PATH/data

if [ $# -lt 3 ] 
then
    echo "Not enough args"
    exit 1
fi


# Download replays
echo "Downloading replays"
mkdir -p $REPLAY_PATH
nolimit node replay-download/download.js $HERO_ID $NUM_PLAYERS $NUM_GAMES
find $REPLAY_PATH -name "*.bz2" -size -2k -delete

# Parse replays
echo "Parsing replays"
mkdir -p $DATA_PATH/{train,test}/{mouseaction,mousesequence,playerstats,iteminfo}

nolimit java -jar dota-parser/target/dota-parser-1.0.jar $REPLAY_PATH/train && mv $DATA_PATH/*.csv $DATA_PATH/train && nolimit java -jar dota-parser/target/dota-parser-1.0.jar $REPLAY_PATH/test && mv $DATA_PATH/*.csv $DATA_PATH/test


echo "Move data"
cd $DATA_PATH/train

find . -name "*mouseaction.csv" -size -10k -delete
find . -name "*mousesequence.csv" -size -10k -delete
find . -name "*playerstats.csv" -size -300c -delete
find . -name "*iteminfo.csv" -size -200c -delete
mv *mouseaction.csv mouseaction/
mv *mousesequence.csv mousesequence/
mv *playerstats.csv playerstats/
mv *iteminfo.csv iteminfo/

cd $DATA_PATH/test

find . -name "*mouseaction.csv" -size -10k -delete
find . -name "*mousesequence.csv" -size -10k -delete
find . -name "*playerstats.csv" -size -300c -delete
find . -name "*iteminfo.csv" -size -200c -delete
mv *mouseaction.csv mouseaction/
mv *mousesequence.csv mousesequence/
mv *playerstats.csv playerstats/
mv *iteminfo.csv iteminfo/

