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
    cmd = "java -jar target/floodlight.jar -cf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/very-simple-net/mininet/floodlightdefault.properties"
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
    s1 = net.addSwitch( 's1', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:01' )
    s2 = net.addSwitch( 's2', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:02' )

    info( '*** Add hosts\n')
    hostsNumber = 3
    userOneHost = net.addHost( 'User1', cls=Host, ip='10.0.0.101/24', cpu=.1/hostsNumber )

    httpLsHost = net.addHost( 'HTTP_LS', cls=Host, ip='10.0.0.1/24', cpu=.1/hostsNumber )
    httpSsHost = net.addHost( 'HTTP_SS', cls=Host, ip='10.0.0.2/24', cpu=.1/hostsNumber )

    info( '*** Add links\n')
    # Add links between switches
    net.addLink( s1, s2, cls=TCLink , bw=10 )

    # Add links to services
    net.addLink( s2, httpLsHost, cls=TCLink , bw=10 )
    net.addLink( s2, httpSsHost, cls=TCLink , bw=10 )

    #Add links to users
    net.addLink( s1, userOneHost, cls=TCLink , bw=10 )

    info( '*** Starting network\n')
    net.build()

    info( '*** Starting switches\n')
    net.get('s1').start([c0])
    net.get('s2').start([c0])

    info( '*** Sleep 15 seconds to let controller get topology...\n')
    sleep(15)

    popens = {}

    info( '*** Starting services\n')

    httpLsCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/very-simple-net/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/very-simple-net/mininet/services.json --logging.file=./http-ls.log --exitStatsFile=./http-ls-exit.xlsx'
    popens[httpLsHost] = httpLsHost.popen(httpLsCommand.split())

    httpSsCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/very-simple-net/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/very-simple-net/mininet/services.json --logging.file=./http-ss.log --exitStatsFile=./http-ss-exit.xlsx'
    popens[httpSsHost] = httpSsHost.popen(httpSsCommand.split())

    info( '*** Sleep 30 seconds to let services start...\n')
    sleep(30)

    info( '*** Starting requests generators...\n')

    userOneCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/very-simple-net/mininet/services.json -lf user-one -st ./user-one-exit.xlsx -er ./user-one-every-request.xlsx -s 11111 -g poisson -l ' + str(lam)
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

