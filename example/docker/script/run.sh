TOPIC=$1

/app/gnatsd &
/app/scheduled.nats.trigger -t $TOPIC
