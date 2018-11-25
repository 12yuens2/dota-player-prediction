function add_words {
    ((count = $(detex $1 | wc -w) ))
    printf "%-30s %s\n" "$1:" "$count"

    (($2 = $2 + count))
}

((words=0))

subfiles=("introduction.tex" "context-survey.tex" "software-engineering.tex" "methodology.tex" "results.tex" "evaluation.tex")
for subfile in ${subfiles[@]}; do
    add_words $subfile "words"
done

printf "\n%-30s %s\n" "Total:" "$words"
