#!/bin/bash
sysctl -w net.ipv4.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0
ip address add 1.0.1.21/24 dev eth3
sysctl -w net.ipv4.conf.eth3.forwarding=1
sysctl -w net.ipv4.conf.eth3.rp_filter=0

sysctl -w net.ipv6.conf.eth3.disable_ipv6=0
sysctl -w net.ipv6.conf.eth3.autoconf=0
ip address add 2000:0:1:1:201:ff:fe00:66/64 dev eth3
ip link set eth3 up
