#!/bin/bash
sysctl -w net.ipv4.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0
ip address add 10.0.0.4/24 dev eth0
sysctl -w net.ipv4.conf.eth0.forwarding=1
sysctl -w net.ipv4.conf.eth0.rp_filter=0

sysctl -w net.ipv6.conf.eth0.disable_ipv6=0
sysctl -w net.ipv6.conf.eth0.autoconf=0
ip address add 2000:0:2:2:201:ff:fe00:4d/64 dev eth0
ip link set eth0 up
