TOPIC=$1
MSG=$2

echo topic is $TOPIC
echo msg is $MSG

/app/gnatsd &
/app/scheduled.nats.trigger -t $TOPIC -m $MSG
