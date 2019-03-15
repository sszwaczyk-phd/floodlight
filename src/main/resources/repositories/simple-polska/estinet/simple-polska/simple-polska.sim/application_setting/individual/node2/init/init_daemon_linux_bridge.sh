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
mstpctl setforcevers br0 rstp
