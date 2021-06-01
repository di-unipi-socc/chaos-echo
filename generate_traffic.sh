#!/bin/bash

# --------------------
# RUN CONFIGURATION
# --------------------
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
	curl -X POST -H 'Content-Type: application/json' -i http://localhost:8080/echo --data '{ "content": "FRONTEND REQUEST", "hash": "1" }'
	sleep $P
done
