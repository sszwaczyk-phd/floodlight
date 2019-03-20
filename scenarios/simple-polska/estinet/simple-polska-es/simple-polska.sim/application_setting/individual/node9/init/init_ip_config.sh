#!/bin/bash
sysctl -w net.ipv4.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0
ip address add 1.0.1.7/24 dev eth5
sysctl -w net.ipv4.conf.eth5.forwarding=1
sysctl -w net.ipv4.conf.eth5.rp_filter=0
sysctl -w net.ipv6.conf.eth5.disable_ipv6=1
sysctl -w net.ipv6.conf.eth5.autoconf=0

ip link set eth5 up
