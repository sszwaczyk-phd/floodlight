#!/bin/bash
ip route flush table main
ip route add 10.0.0.0/24 dev eth0

