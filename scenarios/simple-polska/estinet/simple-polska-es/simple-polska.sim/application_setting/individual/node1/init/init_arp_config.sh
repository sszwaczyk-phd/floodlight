#!/bin/bash
echo 30 > /proc/sys/net/ipv4/neigh/eth0/base_reachable_time
ip neigh add 1.0.1.1 lladdr 00:01:00:00:00:01 nud permanent dev eth0
ip neigh add 1.0.1.2 lladdr 00:01:00:00:00:5d nud permanent dev eth0
ip neigh add 1.0.1.3 lladdr 00:01:00:00:00:57 nud permanent dev eth0
ip neigh add 1.0.1.4 lladdr 00:01:00:00:00:56 nud permanent dev eth0
ip neigh add 1.0.1.5 lladdr 00:01:00:00:00:5a nud permanent dev eth0
ip neigh add 1.0.1.6 lladdr 00:01:00:00:00:5b nud permanent dev eth0
ip neigh add 1.0.1.7 lladdr 00:01:00:00:00:5f nud permanent dev eth0
ip neigh add 1.0.1.8 lladdr 00:01:00:00:00:5c nud permanent dev eth0
ip neigh add 1.0.1.9 lladdr 00:01:00:00:00:5e nud permanent dev eth0
ip neigh add 1.0.1.10 lladdr 00:01:00:00:00:60 nud permanent dev eth0
ip neigh add 1.0.1.11 lladdr 00:01:00:00:00:59 nud permanent dev eth0
ip neigh add 1.0.1.12 lladdr 00:01:00:00:00:58 nud permanent dev eth0
ip neigh add 1.0.1.13 lladdr 00:01:00:00:00:55 nud permanent dev eth0
