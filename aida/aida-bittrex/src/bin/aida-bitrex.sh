#!/bin/sh

SERVICE_NAME=aida-bitrex
JAVA_PROGRAM="java -server -cp /opt/aida-bitrex/lib/*:/opt/aida-bitrex/. ru.aida.bitrex.Module"
PID_PATH_NAME=/opt/okex-storage/aida-okex-storage.pid
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup $JAVA_PROGRAM >> /opt/aida-bitrex/log/aida-bitrex.log 2>&1&
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
        else
            echo "$SERVICE_NAME is already running ..."
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill -15 $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill -15 $PID;
            sleep 3s
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."            
            nohup $JAVA_PROGRAM >> /opt/aida-bitrex/log/aida-bitrex.log 2>&1&
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
            # tail -f ./log/aida-bitrex.log
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
