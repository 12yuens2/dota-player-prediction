HERO_ID=$1
SAVE_PATH=/cs/scratch/sy35/dota-data/$HERO_ID

mkdir -p $SAVE_PATH
for var in "$@" ; do
	if [[ $var != $1 ]] ; then
		echo $var
		node download.js $HERO_ID $var
	
		mkdir -p $SAVE_PATH/$var
		mv *.dem.bz2 $SAVE_PATH/$var
	fi
done
