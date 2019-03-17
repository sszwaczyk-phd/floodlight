#!/bin/bash
mstpd
sleep 1
brctl stp br0 on
ip link set br0 up
mstpctl sethello br0 2
mstpctl setmaxage br0 20
mstpctl setfdelay br0 15
mstpctl setageing br0 3000
mstpctl settreeprio br0 0 8
mstpctl setportpathcost br0 eth0 2000000
mstpctl setportpathcost br0 eth1 2000000
mstpctl setportpathcost br0 eth2 2000000
mstpctl setportpathcost br0 eth3 2000000
mstpctl setportpathcost br0 eth4 2000000
mstpctl setportpathcost br0 eth5 2000000
mstpctl setportpathcost br0 eth6 2000000
mstpctl setportpathcost br0 eth7 2000000
mstpctl setportpathcost br0 eth8 2000000
mstpctl setportpathcost br0 eth9 2000000
mstpctl setportpathcost br0 eth10 2000000
mstpctl setportpathcost br0 eth11 2000000
mstpctl setportpathcost br0 eth12 2000000
mstpctl setportpathcost br0 eth13 2000000
mstpctl setportpathcost br0 eth14 2000000
mstpctl setportpathcost br0 eth15 2000000
mstpctl setportpathcost br0 eth16 2000000
mstpctl setportpathcost br0 eth17 2000000
mstpctl setportpathcost br0 eth18 2000000
mstpctl setportpathcost br0 eth19 2000000
mstpctl setportpathcost br0 eth20 2000000
mstpctl setportpathcost br0 eth21 2000000
mstpctl setportpathcost br0 eth22 2000000
mstpctl setportpathcost br0 eth23 2000000
mstpctl setportpathcost br0 eth24 2000000
mstpctl setportpathcost br0 eth25 2000000
mstpctl setportpathcost br0 eth26 2000000
mstpctl setportpathcost br0 eth27 2000000
mstpctl setportpathcost br0 eth28 2000000
mstpctl setforcevers br0 rstp
