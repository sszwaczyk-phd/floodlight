#!/bin/bash
echo 30 > /proc/sys/net/ipv4/neigh/eth0/base_reachable_time
ip neigh add 10.0.0.1 lladdr 00:01:00:00:00:4b nud permanent dev eth0
ip neigh add 10.0.0.102 lladdr 00:01:00:00:00:50 nud permanent dev eth0
ip neigh add 10.0.0.101 lladdr 00:01:00:00:00:51 nud permanent dev eth0
ip neigh add 10.0.0.103 lladdr 00:01:00:00:00:53 nud permanent dev eth0
ip neigh add 10.0.0.104 lladdr 00:01:00:00:00:52 nud permanent dev eth0
ip neigh add 10.0.0.2 lladdr 00:01:00:00:00:4c nud permanent dev eth0
ip neigh add 10.0.0.3 lladdr 00:01:00:00:00:4e nud permanent dev eth0
ip neigh add 10.0.0.4 lladdr 00:01:00:00:00:4d nud permanent dev eth0
ip neigh add 10.0.0.5 lladdr 00:01:00:00:00:4f nud permanent dev eth0
ip neigh add 10.0.0.105 lladdr 00:01:00:00:00:54 nud permanent dev eth0
ip neigh add 10.0.0.107 lladdr 00:01:00:00:00:56 nud permanent dev eth0
ip neigh add 10.0.0.106 lladdr 00:01:00:00:00:55 nud permanent dev eth0
