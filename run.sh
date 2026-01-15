#!/bin/bash
LOG_DIR="/home/paul/Work/MSA_ELFiN/logs"
LOG_FILE="$LOG_DIR/summary-data-service.log"
mkdir -p "$LOG_DIR"

cd /home/paul/Work/MSA_ELFiN/Re_ELFiN_Summary-Data-Service
nohup sbt -Dconfig.resource=local1.conf run > "$LOG_FILE" 2>&1 &
echo "Summary-Data-Service started with PID: $!"
echo "Log file: $LOG_FILE"
