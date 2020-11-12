#!/usr/bin/python

from mininet.cli import CLI
from mininet.link import TCLink
from mininet.log import setLogLevel, info
from mininet.net import Mininet
from mininet.node import Host
from mininet.node import OVSKernelSwitch
from mininet.node import RemoteController


def simpleNetwork():

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/24')

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0',
                         controller=RemoteController,
                         ip='172.17.0.1',
                         protocol='tcp',
                         port=6653)

    info( '*** Add switches\n')
    leftSwitch = net.addSwitch('s1', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:01')
    upSwitch = net.addSwitch('s2', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:02')
    downSwitch = net.addSwitch('s3', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:03')
    rightSwitch = net.addSwitch('s4', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:04')

    info( '*** Add hosts\n')
    userOneHost = net.addHost( 'User1', cls=Host, ip='10.0.0.1/24' )
    userTwoHost = net.addHost( 'User2', cls=Host, ip='10.0.0.2/24' )
    serviceOneHost = net.addHost( 'Service1', cls=Host, ip='10.0.0.100/24' )

    info( '*** Add links\n')
    net.addLink( userOneHost, leftSwitch, cls=TCLink , bw=10 )
    net.addLink( userTwoHost, upSwitch, cls=TCLink , bw=10 )
    net.addLink( leftSwitch, upSwitch, cls=TCLink , bw=10 )
    net.addLink( leftSwitch, downSwitch, cls=TCLink , bw=10 )
    net.addLink( upSwitch, rightSwitch, cls=TCLink , bw=10 )
    net.addLink( downSwitch, rightSwitch, cls=TCLink , bw=10 )
    net.addLink( rightSwitch, serviceOneHost, cls=TCLink , bw=10 )

    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()

    info( '*** Starting switches\n')
    net.get('s1').start([c0])
    net.get('s2').start([c0])
    net.get('s3').start([c0])
    net.get('s4').start([c0])

    info( '*** Post configure switches and hosts\n')
    serviceOneHost.cmdPrint('java -jar /impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/impl/floodlight/scenarios/simple-net/users.json &')

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    simpleNetwork()