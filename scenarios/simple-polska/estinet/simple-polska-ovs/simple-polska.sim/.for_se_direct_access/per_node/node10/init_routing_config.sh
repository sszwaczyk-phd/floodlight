#!/bin/bash
ip route flush table main
ip route add 1.0.1.0/24 dev eth0

