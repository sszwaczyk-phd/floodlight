#!/bin/bash
sysctl -w net.ipv4.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0
ip address add 10.0.0.104/24 dev eth0
sysctl -w net.ipv4.conf.eth0.forwarding=1
sysctl -w net.ipv4.conf.eth0.rp_filter=0
sysctl -w net.ipv6.conf.eth0.disable_ipv6=1
sysctl -w net.ipv6.conf.eth0.autoconf=0

ip link set eth0 up
