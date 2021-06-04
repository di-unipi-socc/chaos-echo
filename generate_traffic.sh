#!/bin/bash

# --------------------
# RUN CONFIGURATION
# --------------------
A=localhost:8080 # address (and port) where to reach the application frontend
N=1000 # amount of CURL requests to send to the application frontend (default value)
P=0.001 # time interval, in seconds, between requests (default value)

# check if options (-n, -p) are specified to customise run configuration
while (( "$#" )); do
	case "$1" in
		-n|--number)
			if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
				N=$2
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
				exit 2
			fi
		;;
		-a|--address)
			if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
				A=$2
				shift 2
			else
				echo "ERROR: Argument for $1 is missing" >&2
				exit 2
			fi
		*)
			echo "ERROR: Unsupported flag $1" >&2
			echo ""
			echo "    Usage: ./generate_traffic [-n|--number N] [-p|--period M]"
			echo ""
			exit 3
		;;
	esac
done

# --------------------
# TRAFFIC GENERATION
# --------------------

for i in $(seq $N); do
	curl -X POST -H 'Content-Type: application/json' \
		-i http://$A/echo \
		--data '{ "content": "FRONTEND REQUEST", "hash": "1" }' \
		&> /dev/null &
	echo "Request $i done"
	sleep $P
done
