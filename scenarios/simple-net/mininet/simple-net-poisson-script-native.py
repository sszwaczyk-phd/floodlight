#!/usr/bin/python
from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from mininet.util import pmonitor
from subprocess import call

from time import sleep
from time import time
from signal import SIGINT
from signal import SIGTERM

import os
import signal
import subprocess

import sys


def verySimpleNet():

    if len(sys.argv) < 3:
        info( '*** Specify time [s] of simulation and lambda for poisson generators (requests/60s)! --> python very-simple-net-script.py <time> <lambda>...\n')
        return

    simulationTime = int(sys.argv[1])
    lam = float(sys.argv[2])

    info( '*** Starting controller...\n')
    cmd = "java -jar target/floodlight.jar -cf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-net/mininet/floodlightdefault.properties"
    proc = subprocess.Popen(cmd.split(), cwd='/home/sszwaczyk/WAT/PhD/impl/floodlight')

    info( '*** Sleep 5 seconds to let controller start...\n')
    sleep(5)

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/24',
                   autoSetMacs=True,
                   autoStaticArp=True,
                   host=CPULimitedHost)

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0',
                         controller=RemoteController,
                         ip='127.0.0.1',
                         protocol='tcp',
                         port=6653)

    info( '*** Add switches\n')
    leftSwitch = net.addSwitch( 'leftSwitch', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:01' )
    upSwitch = net.addSwitch( 'upSwitch', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:02' )
    downSwitch = net.addSwitch( 'downSwitch', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:03' )
    rightSwitch = net.addSwitch( 'rightSwitch', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:04' )

    info( '*** Add hosts\n')
    hostsNumber = 2
    userOneHost = net.addHost( 'User1', cls=Host, ip='10.0.0.101/24', cpu=.1/hostsNumber )

    httpLsHost = net.addHost( 'HTTP_LS', cls=Host, ip='10.0.0.1/24', cpu=.1/hostsNumber )

    info( '*** Add links\n')
    # Add links between switches
    net.addLink( leftSwitch, upSwitch, cls=TCLink , bw=10 )
    net.addLink( leftSwitch, downSwitch, cls=TCLink , bw=10 )

    net.addLink( upSwitch, rightSwitch, cls=TCLink , bw=10 )

    net.addLink( downSwitch, rightSwitch, cls=TCLink , bw=10 )

    # Add links to services
    net.addLink( rightSwitch, httpLsHost, cls=TCLink , bw=10 )

    #Add links to users
    net.addLink( leftSwitch, userOneHost, cls=TCLink , bw=10 )

    info( '*** Starting network\n')
    net.build()

    info( '*** Starting switches\n')
    net.get('leftSwitch').start([c0])
    net.get('upSwitch').start([c0])
    net.get('downSwitch').start([c0])
    net.get('rightSwitch').start([c0])

    info( '*** Sleep 30 seconds to let controller get topology...\n')
    sleep(30)

    popens = {}

    info( '*** Starting services\n')

    httpLsCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-net/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-net/mininet/services.json --logging.file=./service-one.log --exitStatsFile=./service-one-exit.xlsx'
    popens[httpLsHost] = httpLsHost.popen(httpLsCommand.split())

    info( '*** Sleep 30 seconds to let services start...\n')
    sleep(30)

    info( '*** Starting requests generators...\n')

    userOneCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-net/mininet/services.json -lf user-one -st ./user-one-exit.xlsx -er ./user-one-every-request.xlsx -s 1 -g poisson -l ' + str(lam)
    popens[userOneHost] = userOneHost.popen(userOneCommand.split())

    info( "Simulating for", simulationTime, "seconds\n" )
    endTime = time() + simulationTime

    for h, line in pmonitor( popens, timeoutms=500 ):
        if h:
            info( '<%s>: %s' % ( h.name, line ) )
        if time() >= endTime:
            for p in popens.values():
                info( '*** Stopping requests generators and services...\n')
                p.send_signal( SIGTERM )

    info( '*** Stopping net...\n')
    net.stop()

    info( '*** Stopping controller...\n')
    os.kill(proc.pid, signal.SIGTERM)

if __name__ == '__main__':
    setLogLevel( 'info' )

    verySimpleNet()

