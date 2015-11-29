// Copyright 2013-2015 Apcera Inc. All rights reserved.

package server

import (
	"crypto/tls"
	"net/url"
	"reflect"
	"testing"
	"time"
)

func TestDefaultOptions(t *testing.T) {
	golden := &Options{
		Host:               DEFAULT_HOST,
		Port:               DEFAULT_PORT,
		MaxConn:            DEFAULT_MAX_CONNECTIONS,
		PingInterval:       DEFAULT_PING_INTERVAL,
		MaxPingsOut:        DEFAULT_PING_MAX_OUT,
		TLSTimeout:         float64(SSL_TIMEOUT) / float64(time.Second),
		AuthTimeout:        float64(AUTH_TIMEOUT) / float64(time.Second),
		MaxControlLine:     MAX_CONTROL_LINE_SIZE,
		MaxPayload:         MAX_PAYLOAD_SIZE,
		MaxPending:         MAX_PENDING_SIZE,
		ClusterAuthTimeout: float64(AUTH_TIMEOUT) / float64(time.Second),
		BufSize:            DEFAULT_BUF_SIZE,
	}

	opts := &Options{}
	processOptions(opts)

	if !reflect.DeepEqual(golden, opts) {
		t.Fatalf("Default Options are incorrect.\nexpected: %+v\ngot: %+v",
			golden, opts)
	}
}

func TestOptions_RandomPort(t *testing.T) {
	opts := &Options{Port: RANDOM_PORT}
	processOptions(opts)

	if opts.Port != 0 {
		t.Fatalf("Process of options should have resolved random port to "+
			"zero.\nexpected: %d\ngot: %d\n", 0, opts.Port)
	}
}

func TestConfigFile(t *testing.T) {
	golden := &Options{
		Host:           "apcera.me",
		Port:           4242,
		Username:       "derek",
		Password:       "bella",
		AuthTimeout:    1.0,
		Debug:          false,
		Trace:          true,
		Logtime:        false,
		HTTPPort:       8222,
		LogFile:        "/tmp/gnatsd.log",
		PidFile:        "/tmp/gnatsd.pid",
		ProfPort:       6543,
		Syslog:         true,
		RemoteSyslog:   "udp://foo.com:33",
		MaxControlLine: 2048,
		MaxPayload:     65536,
		MaxConn:        100,
		MaxPending:     10000000,
	}

	opts, err := ProcessConfigFile("./configs/test.conf")
	if err != nil {
		t.Fatalf("Received an error reading config file: %v\n", err)
	}

	if !reflect.DeepEqual(golden, opts) {
		t.Fatalf("Options are incorrect.\nexpected: %+v\ngot: %+v",
			golden, opts)
	}
}

func TestTLSConfigFile(t *testing.T) {
	golden := &Options{
		Host:        "apcera.me",
		Port:        4443,
		Username:    "derek",
		Password:    "buckley",
		AuthTimeout: 1.0,
	}
	opts, err := ProcessConfigFile("./configs/tls.conf")
	if err != nil {
		t.Fatalf("Received an error reading config file: %v\n", err)
	}
	tlsConfig := opts.TLSConfig
	if tlsConfig == nil {
		t.Fatal("Expected opts.TLSConfig to be non-nil")
	}
	opts.TLSConfig = nil
	if !reflect.DeepEqual(golden, opts) {
		t.Fatalf("Options are incorrect.\nexpected: %+v\ngot: %+v",
			golden, opts)
	}
	// Now check TLSConfig a bit more closely
	// CipherSuites
	ciphers := []uint16{
		//		tls.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
		tls.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
		//		tls.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
		tls.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
	}
	if !reflect.DeepEqual(tlsConfig.CipherSuites, ciphers) {
		t.Fatalf("Got incorrect cipher suite list: [%+v]", tlsConfig.CipherSuites)
	}
	if tlsConfig.MinVersion != tls.VersionTLS12 {
		t.Fatalf("Expected MinVersion of 1.2 [%v], got [%v]", tls.VersionTLS12, tlsConfig.MinVersion)
	}
	if tlsConfig.PreferServerCipherSuites != true {
		t.Fatal("Expected PreferServerCipherSuites to be true")
	}
	// Verify hostname is correct in certificate
	if len(tlsConfig.Certificates) != 1 {
		t.Fatal("Expected 1 certificate")
	}
	cert := tlsConfig.Certificates[0].Leaf
	if err := cert.VerifyHostname("localhost"); err != nil {
		t.Fatalf("Could not verify hostname in certificate: %v\n", err)
	}
}

func TestMergeOverrides(t *testing.T) {
	golden := &Options{
		Host:           "apcera.me",
		Port:           2222,
		Username:       "derek",
		Password:       "spooky",
		AuthTimeout:    1.0,
		Debug:          true,
		Trace:          true,
		Logtime:        false,
		HTTPPort:       DEFAULT_HTTP_PORT,
		LogFile:        "/tmp/gnatsd.log",
		PidFile:        "/tmp/gnatsd.pid",
		ProfPort:       6789,
		Syslog:         true,
		RemoteSyslog:   "udp://foo.com:33",
		MaxControlLine: 2048,
		MaxPayload:     65536,
		MaxConn:        100,
		MaxPending:     10000000,
	}
	fopts, err := ProcessConfigFile("./configs/test.conf")
	if err != nil {
		t.Fatalf("Received an error reading config file: %v\n", err)
	}

	// Overrides via flags
	opts := &Options{
		Port:     2222,
		Password: "spooky",
		Debug:    true,
		HTTPPort: DEFAULT_HTTP_PORT,
		ProfPort: 6789,
	}
	merged := MergeOptions(fopts, opts)

	if !reflect.DeepEqual(golden, merged) {
		t.Fatalf("Options are incorrect.\nexpected: %+v\ngot: %+v",
			golden, merged)
	}
}

func TestRemoveSelfReference(t *testing.T) {
	url1, _ := url.Parse("nats-route://user:password@10.4.5.6:4223")
	url2, _ := url.Parse("nats-route://user:password@localhost:4223")
	url3, _ := url.Parse("nats-route://user:password@127.0.0.1:4223")

	routes := []*url.URL{url1, url2, url3}

	newroutes, err := RemoveSelfReference(4223, routes)
	if err != nil {
		t.Fatalf("Error during RemoveSelfReference: %v", err)
	}

	if len(newroutes) != 1 {
		t.Fatalf("Wrong number of routes: %d", len(newroutes))
	}

	if newroutes[0] != routes[0] {
		t.Fatalf("Self reference IP address %s in Routes", routes[0])
	}
}

func TestAllowRouteWithDifferentPort(t *testing.T) {
	url1, _ := url.Parse("nats-route://user:password@127.0.0.1:4224")
	routes := []*url.URL{url1}

	newroutes, err := RemoveSelfReference(4223, routes)
	if err != nil {
		t.Fatalf("Error during RemoveSelfReference: %v", err)
	}

	if len(newroutes) != 1 {
		t.Fatalf("Wrong number of routes: %d", len(newroutes))
	}
}

func TestRouteFlagOverride(t *testing.T) {
	routeFlag := "nats-route://ruser:top_secret@127.0.0.1:8246"
	rurl, _ := url.Parse(routeFlag)

	golden := &Options{
		Port:               7222,
		ClusterHost:        "127.0.0.1",
		ClusterPort:        7244,
		ClusterUsername:    "ruser",
		ClusterPassword:    "top_secret",
		ClusterAuthTimeout: 0.5,
		Routes:             []*url.URL{rurl},
		RoutesStr:          routeFlag,
	}

	fopts, err := ProcessConfigFile("./configs/srv_a.conf")
	if err != nil {
		t.Fatalf("Received an error reading config file: %v\n", err)
	}

	// Overrides via flags
	opts := &Options{
		RoutesStr: routeFlag,
	}
	merged := MergeOptions(fopts, opts)

	if !reflect.DeepEqual(golden, merged) {
		t.Fatalf("Options are incorrect.\nexpected: %+v\ngot: %+v",
			golden, merged)
	}
}

func TestRouteFlagOverrideWithMultiple(t *testing.T) {
	routeFlag := "nats-route://ruser:top_secret@127.0.0.1:8246, nats-route://ruser:top_secret@127.0.0.1:8266"
	rurls := RoutesFromStr(routeFlag)

	golden := &Options{
		Port:               7222,
		ClusterHost:        "127.0.0.1",
		ClusterPort:        7244,
		ClusterUsername:    "ruser",
		ClusterPassword:    "top_secret",
		ClusterAuthTimeout: 0.5,
		Routes:             rurls,
		RoutesStr:          routeFlag,
	}

	fopts, err := ProcessConfigFile("./configs/srv_a.conf")
	if err != nil {
		t.Fatalf("Received an error reading config file: %v\n", err)
	}

	// Overrides via flags
	opts := &Options{
		RoutesStr: routeFlag,
	}
	merged := MergeOptions(fopts, opts)

	if !reflect.DeepEqual(golden, merged) {
		t.Fatalf("Options are incorrect.\nexpected: %+v\ngot: %+v",
			golden, merged)
	}
}
