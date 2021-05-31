FROM alpine:3.13.5
RUN apk add openjdk11
RUN apk add maven 
COPY . /chaos-echo
WORKDIR /chaos-echo
RUN mvn install
CMD java -jar target/chaos-echo-1.jar



