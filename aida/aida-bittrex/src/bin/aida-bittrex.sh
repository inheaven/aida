#!/bin/sh

SERVICE_NAME=aida-bittrex
JAVA_PROGRAM="java -server -cp /opt/aida-bittrex/lib/*:/opt/aida-bittrex/. ru.aida.bittrex.Module"
PID_PATH_NAME=/opt/aida-bittrex/aida-bittrex.pid
case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup $JAVA_PROGRAM >> /opt/aida-bittrex/aida-bittrex.log 2>&1&
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
            nohup $JAVA_PROGRAM >> /opt/aida-bittrex/aida-bittrex.log 2>&1&
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started ..."
            # tail -f ./log/aida-bitrex.log
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
