#!/bin/bash
ip route flush table main
ip route add 10.0.0.0/24 dev eth0
ip -6 route add 2000:0:2:2:0:0:0:0/64 dev eth0

