#!/bin/bash

DIR=`dirname $0`
PID_FILE="pid.run"

pushd $DIR

if [ ! -f $PID_FILE ]
then
    echo "Process not found"
else
    # Get the process ID
    PID=`cat $PID_FILE`

    # Stop the process
    kill $PID
    rm $PID_FILE

    echo "Stopping process $PID"
fi

popd
