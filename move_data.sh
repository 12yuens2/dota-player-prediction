DATA=$1

cd $DATA
mkdir -p ./{mouseaction,mousesequence,playerstats,iteminfo}

mv *-mouseaction.csv mouseaction/
mv *-mousesequence.csv mousesequence/
mv *-playerstats.csv playerstats/
mv *-iteminfo.csv iteminfo
