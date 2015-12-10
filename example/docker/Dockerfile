FROM google/golang
MAINTAINER optimuswu8685@gmail.com

COPY /app /app
COPY /script /script
COPY /nats /go/src

RUN chmod 777 /app/scheduled.nats.trigger & \
    chmod 777 /script/run.sh & \
    go build -o /app/gnatsd /go/src/github.com/apcera/gnatsd/gnatsd.go

EXPOSE 4222

CMD /script/run.sh $TOPIC $MESSAGE 
