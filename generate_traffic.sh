#!/bin/bash

# --------------------
# RUN CONFIGURATION
# --------------------
A=localhost:8080 # address (and port) where to reach the application frontend
N=1000 # amount of CURL requests to send to the application frontend (default value)

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
		-a|--address)
			if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
				A=$2
				shift 2
			else
				echo "ERROR: Argument for $1 is missing" >&2
				exit 2
			fi
		;;
		*)
			echo "ERROR: Unsupported flag $1" >&2
			echo ""
			echo "    Usage: ./generate_traffic [-n|--number N] [-a|--address HOSTNAME:PORT]"
			echo ""
			exit 3
		;;
	esac
done

# --------------------
# TRAFFIC GENERATION
# --------------------

for i in $(seq $N); do
	echo ""
	echo "** Request $i **"
	R=$(curl -X POST -H 'Content-Type: application/json' -H 'Cache-Control: no-cache' -i http://$A/echo --data '{ "content": "FRONTEND REQUEST", "hash": "1" }' --silent)
	while [ -z "$R" ]; do
		sleep 5
		R=$(curl -X POST -H 'Content-Type: application/json' -H 'Cache-Control: no-cache' -i http://$A/echo --data '{ "content": "FRONTEND REQUEST", "hash": "1" }' --silent)
	done
	echo "$R"
done
