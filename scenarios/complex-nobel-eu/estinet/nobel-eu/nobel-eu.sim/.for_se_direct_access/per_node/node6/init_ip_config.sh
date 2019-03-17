#!/bin/bash
sysctl -w net.ipv4.conf.all.forwarding=1
sysctl -w net.ipv4.conf.all.rp_filter=0
ip address add 1.0.1.12/24 dev eth5
sysctl -w net.ipv4.conf.eth5.forwarding=1
sysctl -w net.ipv4.conf.eth5.rp_filter=0

sysctl -w net.ipv6.conf.eth5.disable_ipv6=0
sysctl -w net.ipv6.conf.eth5.autoconf=0
ip address add 2000:0:1:1:201:ff:fe00:2e/64 dev eth5
ip link set eth5 up
