#!/bin/bash

# --------------------
# RUN CONFIGURATION
# --------------------
A=localhost:8080 # address (and port) where to reach the application frontend (default value)
D=3000 # time interval during which to send (non-blocking) CURL requests to the application frontend (default value: 5 minutes)
P=0.01 # period for CURL requests (default value: 1/100 sec)

# check if options (-n, -p) are specified to customise run configuration
while (( "$#" )); do
	case "$1" in
		-a|--address)
			if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
				A=$2
				shift 2
			else
				echo "ERROR: Argument for $1 is missing" >&2
				exit 2
			fi
		;;
		-d|--duration)
			if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
				D=$2
				shift 2
			else
				echo "ERROR: Argument for $1 is missing" >&2
				exit 1
			fi
		;;
		-p|--period)
			if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
				P=$2
				shift 2
			else
				echo "ERROR: Argument for $1 is missing" >&2
				exit 1
			fi
		;;
		*)
			echo "ERROR: Unsupported flag $1" >&2
			echo ""
			echo "    Usage: ./load.sh [-a|--address HOSTNAME:PORT] [-d|--duration SECONDS] [-p|--period SECONDS] "
			echo ""
			exit 3
		;;
	esac
done

# --------------------
# TRAFFIC GENERATION
# --------------------

SECONDS=0
while (( SECONDS <= D )); do  # loop until duration D (seconds) has expired
	curl -X POST -H 'Content-Type: application/json' -H 'Cache-Control: no-cache' -i http://$A/echo --data '{ "content": "FRONTEND REQUEST", "hash": "1" }' --retry 6 --retry-connrefused --silent &
	sleep $P
done
