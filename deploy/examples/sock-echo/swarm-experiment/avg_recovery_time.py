# coding=utf8

import os

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

# Process each log separately
for logFile in failFiles:
    print(logFile) # TODO: process each line of the file separately, as each corresponds to a different json object?
