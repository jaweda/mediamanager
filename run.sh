#!/bin/bash

DIR=`dirname $0`
PID_FILE="pid.run"

pushd $DIR

nohup java -jar mediamanager-*.jar server config.yml >logs/sysout.log 2>&1 &
PID=$!

if [ -z "$PID" ]
then
	echo "MediaManager failed to start"
	tail logs/sysout.log
else
    echo $PID > $PID_FILE
    echo "MediaManager started with process ID $PID"
fi

popd
