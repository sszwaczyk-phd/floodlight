#!/bin/bash
#Node22ovs start script
mkdir /var/run/openvswitch
ovsdb-tool create /etc/openvswitch/conf.db /usr/share/openvswitch/vswitch.ovsschema
ovsdb-server --remote=punix:/var/run/openvswitch/db.sock --remote=db:Open_vSwitch,Open_vSwitch,manager_options --pidfile --detach --log-file
ovs-vsctl --no-wait init
ovs-vswitchd --pidfile --detach --log-file
mkdir /dev/net
mknod /dev/net/tun c 10 200
ovs-vsctl add-br ovs-br0
ovs-vsctl set bridge ovs-br0
ovs-vsctl set controller ovs-br0 connection-mode=out-of-band
ovs-vsctl set bridge ovs-br0 protocols=OpenFlow10,OpenFlow11,OpenFlow12,OpenFlow13
ovs-vsctl set-fail-mode ovs-br0 secure
ovs-vsctl set-controller ovs-br0 tcp:1.0.1.1:6653
ovs-vsctl add-port ovs-br0 eth0
ovs-vsctl set Interface eth0 ofport_request=1
ovs-vsctl add-port ovs-br0 eth1
ovs-vsctl set Interface eth1 ofport_request=2
ovs-vsctl add-port ovs-br0 eth2
ovs-vsctl set Interface eth2 ofport_request=3
ovs-vsctl add-port ovs-br0 eth4
ovs-vsctl set Interface eth4 ofport_request=5
ovs-vsctl set Bridge ovs-br0 mcast_snooping_enable=true
ip link set ovs-br0 up
