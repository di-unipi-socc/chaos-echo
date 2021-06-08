#!/bin/bash

# ---------------------------------------
# Copy deployment files in current folder
# ---------------------------------------
cp ../docker-compose.yml docker-compose.yml
cp ../logstash.conf logstash.conf
cp ../../../../generate_traffic.sh generate_traffic.sh
chmod ugo+x generate_traffic.sh

# --------------------------------------------------------------------
# Generate logs for varying FAIL_PERCENTAGE (with PICK_PERCENTAGE=100)
# --------------------------------------------------------------------
sed -i "s/PICK_PERCENTAGE: 50/PICK_PERCENTAGE: 100/g" docker-compose.yml # set services to always invoke backends

OLD_PERC=5
for PERC in $(seq 5 5 95)
do 
	echo "---- FAIL_PERCENTAGE = $PERC ----"
	# Update docker-compose.yml
	sed -i "s/FAIL_PERCENTAGE: $OLD_PERC/FAIL_PERCENTAGE: $PERC/g" docker-compose.yml
	echo "* Updated docker-compose.yml" 

	# Deploy Docker stack (and wait for services to get online)
	echo "* Docker stack deployment started"
	docker stack deploy -c docker-compose.yml echo
	echo -n "* Waiting for services to get online.." 
	sleep 60
	echo "done!"

	# Load services
	echo -n "* Loading services.."
	./generate_traffic.sh -n 10000 > /dev/null
	echo "done!"

	# Remove Docker stack
	echo "* Undeployment of Docker stack"
	docker stack rm echo
	
	# Rename generate logs
	LOG_FILE=$(ls echo-*)
	mv $LOG_FILE fail_$PERC.log
	echo "* Log file stored in fail_$PERC.log"

	# Cleaning Docker runtime
	echo -n "* Cleaning Docker runtime.."
	docker container prune -f
	docker network prune -f
	sleep 10
	echo "done!"

	OLD_PERC=$PERC
done

# --------------------------------
# Remove (copied) deployment files
# --------------------------------
rm docker-compose.yml logstash.conf generate_traffic.sh
