#!/bin/bash
ip route flush table main
ip route add 0.0.0.0/0 dev eth0
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth0
ip route add 0.0.0.0/0 dev eth1
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth1
ip route add 0.0.0.0/0 dev eth2
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth2
ip route add 0.0.0.0/0 dev eth3
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth3
ip route add 0.0.0.0/0 dev eth4
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth4
ip route add 0.0.0.0/0 dev eth5
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth5
ip route add 0.0.0.0/0 dev eth6
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth6
ip route add 0.0.0.0/0 dev eth7
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth7
ip route add 0.0.0.0/0 dev eth8
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth8
ip route add 0.0.0.0/0 dev eth9
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth9
ip route add 0.0.0.0/0 dev eth10
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth10
ip route add 0.0.0.0/0 dev eth11
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth11
ip route add 0.0.0.0/0 dev eth12
ip -6 route add 2000:0:1:1:0:0:0:0/64 dev eth12

