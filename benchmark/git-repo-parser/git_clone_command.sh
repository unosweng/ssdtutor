#!/bin/bash
count=1
while IFS='' read -r line || [[ -n "$line" ]]; do
   if [ $count -gt 2 ]
   then
      echo break by $count.
      break
   fi
   ((count++))
   $line
done < "$1"
