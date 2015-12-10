package main

import (
	"flag"
	"fmt"
	"os"
	"time"

	"github.com/nats-io/nats"
)

var topic string
var message string

func init() {
	flag.StringVar(&topic, "t", "topic.example.prefix.1", "the name of the topic")
	flag.StringVar(&message, "m", "hello world", "the message of the message")
}

func main() {
	flag.Parse()
	fmt.Println("topic is " + topic)
	fmt.Println("message is " + message)
	ticker := time.NewTicker(4 * time.Second)
	_ = <-ticker.C

	nc, _ := nats.Connect(nats.DefaultURL)
	c, _ := nats.NewEncodedConn(nc, nats.JSON_ENCODER)
	defer c.Close()
	if c == nil {
		fmt.Printf("nats error: fail to connect.\n")
		os.Exit(0)
	}
	for {
		err := c.LastError()
		if err != nil {
			fmt.Printf("nats error: %s", err.Error())
			break
		}

		_ = <-ticker.C
		err = c.Publish(topic, message)
		if err != nil {
			fmt.Printf("publish err: %s", err.Error())
			os.Exit(0)
		}
		fmt.Printf("public topic %s with content %s\n", topic, message)
	}
}

