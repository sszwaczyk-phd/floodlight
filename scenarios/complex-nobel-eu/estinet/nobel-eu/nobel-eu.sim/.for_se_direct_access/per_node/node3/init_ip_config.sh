#!/bin/bash
sysctl -w net.ipv4.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0
ip address add 1.0.1.4/24 dev eth2
sysctl -w net.ipv4.conf.eth2.forwarding=1
sysctl -w net.ipv4.conf.eth2.rp_filter=0

sysctl -w net.ipv6.conf.eth2.disable_ipv6=0
sysctl -w net.ipv6.conf.eth2.autoconf=0
ip address add 2000:0:1:1:201:ff:fe00:21/64 dev eth2
ip link set eth2 up
