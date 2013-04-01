#!/bin/bash

PID_FILE="pid.run"

if [ ! -f $PID_FILE ];
then
        echo "Process not found"
        exit 1
fi

# Get the process ID
PID=`cat $PID_FILE`

# Stop the process
kill $PID
rm $PID_FILE

echo "Stopping process $PID"
