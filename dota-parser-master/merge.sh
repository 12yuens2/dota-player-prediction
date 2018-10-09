# Write headers
cat mouseAction.csv | head -n1 > merged.csv

# Merge
for f in *.csv; do cat "`pwd`/$f" | tail -n +2 >> merged.csv; done
