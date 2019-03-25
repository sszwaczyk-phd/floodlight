#!/bin/bash
sysctl -w net.ipv4.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0
ip address add 1.0.1.8/24 dev eth4
sysctl -w net.ipv4.conf.eth4.forwarding=1
sysctl -w net.ipv4.conf.eth4.rp_filter=0
sysctl -w net.ipv6.conf.eth4.disable_ipv6=1
sysctl -w net.ipv6.conf.eth4.autoconf=0

ip link set eth4 up
