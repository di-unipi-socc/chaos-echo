#!/bin/bash

REPEAT=8

# ---------------------------------------
# Copy deployment files in current folder
# ---------------------------------------
cp ../docker-compose.yml docker-compose.yml
cp ../logstash.conf logstash.conf
cp ../../../../load.sh load.sh
chmod ugo+x load.sh

sed -i "s/PICK_PERCENTAGE: 50/PICK_PERCENTAGE: 100/g" docker-compose.yml # set services to always invoke backends

# -----------------------------------------
# Generate logs for varying FAIL_PERCENTAGE
# -----------------------------------------

OLD_PERC=5
for PERC in $(seq 10 10 90)
do
	echo "|-------------------------------|"
	echo "|     FAIL_PERCENTAGE = $PERC      |"
	echo "|-------------------------------|"
	# Update docker-compose.yml
	echo "* Update docker-compose.yml"
	sed -i "s/FAIL_PERCENTAGE: $OLD_PERC/FAIL_PERCENTAGE: $PERC/g" docker-compose.yml

	# Deploy Docker stack (and wait for services to get online)
	echo "* Docker stack deployment started"
	docker stack deploy -c docker-compose.yml echo
	echo "* Waiting for ELK stack to get online"
	sleep 100

	# Load services
	echo "* Loading services"
	./load.sh -d 1200 > /dev/null
	sleep 180

	# Remove Docker stack
	echo "* Undeployment of Docker stack"
	docker stack rm echo

	# Rename generate logs
	LOG_FILE=$(ls echo-*)
	mv $LOG_FILE fail_perc_$PERC.log
	echo "* Log file stored in fail_perc_$PERC.log"

	# Cleaning Docker runtime
	echo "* Cleaning Docker runtime"
	docker container prune -f
	docker network prune -f
	sleep 30

	OLD_PERC=$PERC
done

# --------------------------------
# Process (and clean) obtained logs
# --------------------------------
python3 avg_recovery_time.py > percentage_$REPEAT.csv
mkdir run_$REPEAT
mv fail_* run_$REPEAT

# -----------------------------------------
#    Generate logs for varying REPLICAS
# -----------------------------------------

sed -i "s/FAIL_PERCENTAGE: $OLD_PERC/FAIL_PERCENTAGE: 50/g" docker-compose.yml # set 50 as FAIL_PERCENTAGE
OLD_REPLICAS=1
for REPLICAS in $(seq 1 1 10)
do
	echo "|-------------------------------|"
	echo "|     REPLICAS = $REPLICAS      |"
	echo "|-------------------------------|"
	# Update docker-compose.yml
	echo "* Update docker-compose.yml"
	sed -i "s/replicas: $OLD_REPLICAS/replicas: $REPLICAS/g" docker-compose.yml
	
	# Deploy Docker stack (and wait for services to get online)
	echo "* Docker stack deployment started"
	docker stack deploy -c docker-compose.yml echo
	echo "* Waiting for ELK stack to get online"
	sleep 600

	# Load services
	echo "* Loading services"
	./load.sh -d 1200 > /dev/null
	sleep 180
	# Remove Docker stack
	echo "* Undeployment of Docker stack"
	docker stack rm echo

	# Rename generate logs
	LOG_FILE=$(ls echo-*)
	mv $LOG_FILE fail_replicas_$REPLICAS.log
	echo "* Log file stored in fail_replicas_$REPLICAS.log"

	# Cleaning Docker runtime
	echo "* Cleaning Docker runtime"
	docker container prune -f
	docker network prune -f
	sleep 30

	OLD_REPLICAS=$REPLICAS
done

# --------------------------------
# Remove (copied) deployment files
# --------------------------------
rm docker-compose.yml logstash.conf load.sh

# --------------------------------
# Process (and clean) obtained logs
# --------------------------------
python3 avg_recovery_time.py > replicas_$REPEAT.csv
mv fail_* run_$REPEAT

done 
