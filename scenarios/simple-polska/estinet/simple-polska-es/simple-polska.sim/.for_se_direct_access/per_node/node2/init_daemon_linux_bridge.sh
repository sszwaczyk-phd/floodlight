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
mstpctl setportpathcost br0 eth0 200000
mstpctl setportpathcost br0 eth1 200000
mstpctl setportpathcost br0 eth2 200000
mstpctl setportpathcost br0 eth3 200000
mstpctl setportpathcost br0 eth4 200000
mstpctl setportpathcost br0 eth5 200000
mstpctl setportpathcost br0 eth6 200000
mstpctl setportpathcost br0 eth7 200000
mstpctl setportpathcost br0 eth8 200000
mstpctl setportpathcost br0 eth9 200000
mstpctl setportpathcost br0 eth10 200000
mstpctl setportpathcost br0 eth11 200000
mstpctl setportpathcost br0 eth12 200000
mstpctl setforcevers br0 rstp
