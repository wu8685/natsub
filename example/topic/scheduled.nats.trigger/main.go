package main

import (
	"flag"
	"fmt"
	"os"
	"time"

	"github.com/nats-io/nats"
)

var topic string

func init() {
	topic = *(flag.String("t", "topic.example.1", "the name of a topic"))
}

func main() {
	nc, _ := nats.Connect(nats.DefaultURL)
	c, _ := nats.NewEncodedConn(nc, nats.JSON_ENCODER)
	defer c.Close()
	if c == nil {
		fmt.Printf("nats error: fail to connect.\n")
		os.Exit(0)
	}

	ticker := time.NewTicker(4 * time.Second)
	for {
		err := c.LastError()
		if err != nil {
			fmt.Printf("nats error: %s", err.Error())
			break
		}

		_ = <-ticker.C
		content := "{\"test\":\"hello world\"}"
		err = c.Publish(topic, content)
		if err != nil {
			fmt.Printf("publish err: %s", err.Error())
			os.Exit(0)
		}
		fmt.Printf("public topic %s with content %s", topic, content)
	}
}
