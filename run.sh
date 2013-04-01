#!/bin/bash

PID_FILE="pid.run"

nohup java -jar mediamanager-*.jar server config.yml >logs/sysout.log 2>&1 &
PID=$!

if [ -z "$PID" ]
then
	echo "MediaManager failed to start"
	exit
fi

echo $PID > $PID_FILE
echo "MediaManager started with process ID $PID"