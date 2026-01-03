#!/bin/bash
set -e

jqcheck=`dpkg -l | grep jq | wc -l`
if [ "0" = $jqcheck ]; then
    echo "This script requires jq to run. Please install it by 'sudo apt-get install jq'." 
    exit 0
fi

printhelp(){
echo "Usage: ./crawl.sh <options>"
echo "Search all Java repositories on Github with supplied options."
echo "Options:"
echo "-h Print help, do not use it with other options."
echo "-s [integer]: Find all repositories with minimum stars."
echo "-f [integer]: Find all repositories with minimum forks."
echo "-t [string]: Find all repositories in a topic."
echo "-p [YYYY-MM-DD]: Find all repositories which were pushed after the specified date. Default value is 2018-01-01."
}
hstmt="The usage is: crawl.sh -h -s min_stars -f min_fork -t topic -l last_commit_date"

# Parse out the options
while getopts ":hn:s:f:t:p:" opt; do
    case $opt in
    h)
        printhelp
        exit 0
        ;;
    s)
        stars=$OPTARG
        ;;
    f)
        forks=$OPTARG
        ;;
    t)
        topic=$OPTARG
        ;;
    p)
        sincedate=$OPTARG
        ;;
    \?)
        echo "Invalid option."
        printhelp
        exit 0
    esac
done
# Since date if has not been set
if [ -z $sincedate ]; then
    sincedate='2018-01-01'
fi
enddate=`date +%Y-%m-%d`

############################################
# Segment the entire time period into every 10 days
sincedates=()
enddates=()
while [ "$sincedate" \< "$enddate" ]
do    
    tmptodate=`date -d "$sincedate+4 days" +'%Y-%m-%d'`
    if [ "$tmptodate" \> "$enddate" ]; then
        tmptodate=$enddate
    fi    
    sincedates+=($sincedate)
    enddates+=($tmptodate)
    sincedate=`date -d "$sincedate+5 days" +'%Y-%m-%d'`       
done

# https://developer.github.com/v3/search/
# For unauthenticated requests, the rate limit allows you to make up to 10 requests per minute.
interval=7s

# File of results
resultcsv="sum.csv"
rm -f $resultcsv
touch $resultcsv

# Prepare dir for temp files and the result file
tmpdir='fetched'
rm -rf $tmpdir
mkdir -p $tmpdir

makecondition () {
    querycond='language:java'
    if [ ! -z $stars ]; then
        querycond=$querycond"+stars:>=$stars+fork:true"
    fi
    if [ ! -z $forks ]; then
        querycond=$querycond"+forks:>=$forks"
    fi
    if [ ! -z $topic ]; then
        querycond="$topic+"$querycond
    fi
    querycond=$querycond"+pushed:$1..$2"
}

makeurl () {
    makecondition $1 $2 $3
    url="https://api.github.com/search/repositories?q="$querycond"&page=$3&per_page=100"
}

maketmppath () {
    name='language:java'
    if [ ! -z $stars ]; then
        name=$name"_stars:$stars"
    fi
    if [ ! -z $forks ]; then
        name=$name"_forks:$forks"
    fi
    if [ ! -z $topic ]; then
        name="topic:${topic}_"$name
    fi
    name=$name"_pushed:$1:$2"
    tmpfile="${tmpdir}/${name}_$3.json"
}

makecommand() {
    # The command should look like curl 'https://api.github.com/search/repositories?q=language:java&page=1&per_page=100' > text1.txt
    makeurl $1 $2 $3
    maketmppath $1 $2 $3
    command="curl '$url' > $tmpfile"
    echo $command
}

executecommand() {
    makecommand $1 $2 $3
    eval $command
}

updateresult() {
    `jq -r '.items[] | "\(.name),\(.clone_url)"' $tmpfile >> $resultcsv`
}

# For every time frame, do the search
crawleditemnum=0
for ((idx=0; idx<${#sincedates[@]}; idx++)); do
    counter=1
    executecommand ${sincedates[idx]} ${enddates[idx]} $counter
    
    # Now need to process the first page at $tmpfile
    itemnum=`jq '.total_count' $tmpfile`
    pagenum=$(($itemnum/100+1))

    # Update the result csv file
    updateresult

    # Continue to work on following pages
    sleep $interval
    for ((counter=2; counter<=$pagenum; counter++)); do
        echo $counter
        executecommand ${sincedates[idx]} ${enddates[idx]} $counter
        updateresult
        sleep $interval
    done

    # Check so far how many repos have been crawled
    crawleditemnum=$(($crawleditemnum+$itemnum))
    echo "+ $itemnum ====> $crawleditemnum repositories have been crawled"
done