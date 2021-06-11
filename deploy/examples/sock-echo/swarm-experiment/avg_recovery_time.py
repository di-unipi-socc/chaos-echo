# coding=utf8

from datetime import datetime
import json
import os

def getInstanceName(containerName):
    # split container name into service, instance number, container id
    splittedInstance = containerName.split(".")
    # keep only service and instance number
    instance = splittedInstance[0] + "." + splittedInstance[1]
    return instance

def getMillisecs(timestamp):
    t = datetime.strptime(timestamp, "%Y-%m-%dT%H:%M:%S.%fZ")
    millisec = t.timestamp() * 1000
    return millisec

# Getting files in "swarm-experiment" directory
files = os.listdir(os.getcwd())

# -----------------------
# VARYING FAIL_PERCENTAGE
# ----------------------- 

# Isolating logs for experiment with varying percentage
failFiles = []
for file in files:
    if file.startswith("fail"):
        failFiles.append(file)

# Process each log file separately
for logFile in failFiles:
    # Using "r" as a dictionary storing a map of recovery intervals for each container
    r = {}
    # Getting timestamps of "Crashing" DEBUG events
    with open(logFile) as logs:
        for line in logs:
            log = json.loads(line)
            instance = getInstanceName(log["container_name"])
            if instance not in r.keys():
                r[instance] = {}
            if log["severity"] == "DEBUG" and log["event"] == "Crashing":
                timestamp = getMillisecs(log["@timestamp"])
                r[instance][timestamp] = None
        logs.close()
    # Getting timestamp of newly logged "ACCEPTING_TRAFFIC" INFO messages, right after each crash
    with open(logFile) as logs:
        for line in logs:
            log = json.loads(line)
            instance = getInstanceName(log["container_name"])
            if (log["severity"] == "INFO") and ("ACCEPTING_TRAFFIC" in log["message"]):
                timestamp = getMillisecs(log["@timestamp"])
                for crashTime in r[instance].keys():
                    if crashTime < timestamp:
                        if (r[instance][crashTime] == None) or (r[instance][crashTime] > timestamp):
                            r[instance][crashTime] = timestamp
        logs.close()
    # Computing average recovery time for current file
    recoveryTime = 0
    recoveryCount = 0
    for instance in r.keys():
        for crashTimestamp in r[instance].keys():
            recoveryTimestamp = r[instance][crashTimestamp]
            if recoveryTimestamp:
                recoveryTime += recoveryTimestamp - crashTimestamp
                recoveryCount += 1
    if recoveryCount > 0:
        averageRecoveryTime = recoveryTime/recoveryCount
        print(logFile + "," + str(averageRecoveryTime))
