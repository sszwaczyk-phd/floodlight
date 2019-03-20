#!/bin/bash
echo 30 > /proc/sys/net/ipv4/neigh/eth0/base_reachable_time
ip neigh add 10.0.0.1 lladdr 00:01:00:00:00:0f nud permanent dev eth0
ip neigh add 10.0.0.101 lladdr 00:01:00:00:00:15 nud permanent dev eth0
ip neigh add 10.0.0.102 lladdr 00:01:00:00:00:14 nud permanent dev eth0
ip neigh add 10.0.0.103 lladdr 00:01:00:00:00:17 nud permanent dev eth0
ip neigh add 10.0.0.2 lladdr 00:01:00:00:00:10 nud permanent dev eth0
ip neigh add 10.0.0.104 lladdr 00:01:00:00:00:16 nud permanent dev eth0
ip neigh add 10.0.0.3 lladdr 00:01:00:00:00:12 nud permanent dev eth0
ip neigh add 10.0.0.5 lladdr 00:01:00:00:00:13 nud permanent dev eth0
ip neigh add 10.0.0.4 lladdr 00:01:00:00:00:11 nud permanent dev eth0
ip neigh add 10.0.0.105 lladdr 00:01:00:00:00:18 nud permanent dev eth0
ip neigh add 10.0.0.107 lladdr 00:01:00:00:00:1a nud permanent dev eth0
ip neigh add 10.0.0.106 lladdr 00:01:00:00:00:19 nud permanent dev eth0
