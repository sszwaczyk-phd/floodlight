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


def complexNobelEu():

    if len(sys.argv) < 3:
        info( '*** Specify time [s] of simulation and lambda for poisson generators (requests/60s)! --> python complex-nobel-eu-script.py <time> <lambda>...\n')
        return

    simulationTime = int(sys.argv[1])
    lam = float(sys.argv[2])

    info( '*** Starting controller...\n')
    cmd = "java -jar target/floodlight.jar -cf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/floodlightdefault.properties"
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
    glasgow = net.addSwitch( 'glasgow', dpid='00:00:00:00:00:00:00:01' )
    dublin = net.addSwitch( 'dublin', dpid='00:00:00:00:00:00:00:02' )
    london = net.addSwitch( 'london', dpid='00:00:00:00:00:00:00:03' )
    amsterdam = net.addSwitch( 'amsterdam', dpid='00:00:00:00:00:00:00:04' )
    brussels = net.addSwitch( 'brussels', dpid='00:00:00:00:00:00:00:05' )
    paris = net.addSwitch( 'paris', dpid='00:00:00:00:00:00:00:06' )
    bordeaux = net.addSwitch( 'bordeaux', dpid='00:00:00:00:00:00:00:07' )
    madrid = net.addSwitch( 'madrid', dpid='00:00:00:00:00:00:00:08' )
    barcelona = net.addSwitch( 'barcelona', dpid='00:00:00:00:00:00:00:09' )
    lyon = net.addSwitch( 'lyon', dpid='00:00:00:00:00:00:00:10' )
    hamburg = net.addSwitch( 'hamburg', dpid='00:00:00:00:00:00:00:11' )
    frankfurt = net.addSwitch( 'frankfurt', dpid='00:00:00:00:00:00:00:12' )
    strasbourg = net.addSwitch( 'strasbourg', dpid='00:00:00:00:00:00:00:13' )
    zurich = net.addSwitch( 'zurich', dpid='00:00:00:00:00:00:00:14' )
    milan = net.addSwitch( 'milan', dpid='00:00:00:00:00:00:00:15' )
    oslo = net.addSwitch( 'oslo', dpid='00:00:00:00:00:00:00:16' )
    copenhagen = net.addSwitch( 'copenhagen', dpid='00:00:00:00:00:00:00:17' )
    berlin = net.addSwitch( 'berlin', dpid='00:00:00:00:00:00:00:18' )
    munich = net.addSwitch( 'munich', dpid='00:00:00:00:00:00:00:19' )
    prague = net.addSwitch( 'prague', dpid='00:00:00:00:00:00:00:20' )
    vienna = net.addSwitch( 'vienna', dpid='00:00:00:00:00:00:00:21' )
    zagreb = net.addSwitch( 'zagreb', dpid='00:00:00:00:00:00:00:22' )
    rome = net.addSwitch( 'rome', dpid='00:00:00:00:00:00:00:23' )
    stockholm = net.addSwitch( 'stockholm', dpid='00:00:00:00:00:00:00:24' )
    warsaw = net.addSwitch( 'warsaw', dpid='00:00:00:00:00:00:00:25' )
    budapest = net.addSwitch( 'budapest', dpid='00:00:00:00:00:00:00:26' )
    belgrade = net.addSwitch( 'belgrade', dpid='00:00:00:00:00:00:00:27' )
    athens = net.addSwitch( 'athens', dpid='00:00:00:00:00:00:00:28' )

    info( '*** Add hosts\n')
    hostsNumber = 21
    userOneHost = net.addHost( 'User1', cls=Host, ip='10.0.0.101/24', cpu=.1/hostsNumber )
    userTwoHost = net.addHost( 'User2', cls=Host, ip='10.0.0.102/24', cpu=.1/hostsNumber )
    userThreeHost = net.addHost( 'User3', cls=Host, ip='10.0.0.103/24', cpu=.1/hostsNumber )
    userFourHost = net.addHost( 'User4', cls=Host, ip='10.0.0.104/24', cpu=.1/hostsNumber )
    userFiveHost = net.addHost( 'User5', cls=Host, ip='10.0.0.105/24', cpu=.1/hostsNumber )
    userSixHost = net.addHost( 'User6', cls=Host, ip='10.0.0.106/24', cpu=.1/hostsNumber )
    userSevenHost = net.addHost( 'User7', cls=Host, ip='10.0.0.107/24', cpu=.1/hostsNumber )

    httpLsHost = net.addHost( 'HTTP_LS', cls=Host, ip='10.0.0.1/24', cpu=.1/hostsNumber )
    httpLrHost = net.addHost( 'HTTP_LR', cls=Host, ip='10.0.0.2/24', cpu=.1/hostsNumber )
    httpSsHost = net.addHost( 'HTTP_SS', cls=Host, ip='10.0.0.3/24', cpu=.1/hostsNumber )
    httpSrHost = net.addHost( 'HTTP_SR', cls=Host, ip='10.0.0.4/24', cpu=.1/hostsNumber )
    httpSuHost = net.addHost( 'HTTP_SU', cls=Host, ip='10.0.0.5/24', cpu=.1/hostsNumber )

    info( '*** Add links\n')
    # Add links between switches
    net.addLink( glasgow, dublin, cls=TCLink ,  bw=10 )
    net.addLink( glasgow, amsterdam, cls=TCLink ,  bw=10 )

    net.addLink( dublin, london, cls=TCLink ,  bw=10 )

    net.addLink( london, amsterdam, cls=TCLink ,  bw=10 )
    net.addLink( london, paris, cls=TCLink ,  bw=10 )

    net.addLink( paris, brussels, cls=TCLink ,  bw=10 )
    net.addLink( paris, bordeaux, cls=TCLink ,  bw=10 )
    net.addLink( paris, strasbourg, cls=TCLink ,  bw=10 )
    net.addLink( paris, lyon, cls=TCLink ,  bw=10 )

    net.addLink( bordeaux, madrid, cls=TCLink ,  bw=10 )

    net.addLink( madrid, barcelona, cls=TCLink ,  bw=10 )

    net.addLink( barcelona, lyon, cls=TCLink ,  bw=10 )

    net.addLink( lyon, zurich, cls=TCLink ,  bw=10 )

    net.addLink( amsterdam, brussels, cls=TCLink ,  bw=10 )
    net.addLink( amsterdam, hamburg, cls=TCLink ,  bw=10 )

    net.addLink( brussels, frankfurt, cls=TCLink ,  bw=10 )

    net.addLink( hamburg, frankfurt, cls=TCLink ,  bw=10 )
    net.addLink( hamburg, berlin, cls=TCLink ,  bw=10 )

    net.addLink( frankfurt, strasbourg, cls=TCLink ,  bw=10 )
    net.addLink( frankfurt, munich, cls=TCLink ,  bw=10 )

    net.addLink( strasbourg, zurich, cls=TCLink ,  bw=10 )

    net.addLink( zurich, milan, cls=TCLink ,  bw=10 )

    net.addLink( milan, munich, cls=TCLink ,  bw=10 )
    net.addLink( milan, rome, cls=TCLink ,  bw=10 )

    net.addLink( oslo, stockholm, cls=TCLink ,  bw=10 )
    net.addLink( oslo, copenhagen, cls=TCLink ,  bw=10 )

    net.addLink( copenhagen, berlin, cls=TCLink ,  bw=10 )

    net.addLink( berlin, warsaw, cls=TCLink ,  bw=10 )
    net.addLink( berlin, prague, cls=TCLink ,  bw=10 )
    net.addLink( berlin, munich, cls=TCLink ,  bw=10 )

    net.addLink( munich, vienna, cls=TCLink ,  bw=10 )

    net.addLink( prague, vienna, cls=TCLink ,  bw=10 )
    net.addLink( prague, budapest, cls=TCLink ,  bw=10 )

    net.addLink( vienna, zagreb, cls=TCLink ,  bw=10 )

    net.addLink( zagreb, rome, cls=TCLink ,  bw=10 )

    net.addLink( rome, athens, cls=TCLink ,  bw=10 )

    net.addLink( stockholm, warsaw, cls=TCLink ,  bw=10 )

    net.addLink( warsaw, budapest, cls=TCLink ,  bw=10 )

    net.addLink( budapest, belgrade, cls=TCLink ,  bw=10 )

    net.addLink( belgrade, athens, cls=TCLink ,  bw=10 )

    # Add links to services
    net.addLink( frankfurt, httpLsHost, cls=TCLink , bw=10 )
    net.addLink( dublin, httpLrHost, cls=TCLink , bw=10 )
    net.addLink( copenhagen, httpSsHost, cls=TCLink , bw=10 )
    net.addLink( belgrade, httpSrHost, cls=TCLink , bw=10 )
    net.addLink( rome, httpSuHost, cls=TCLink , bw=10 )

    #Add links to users
    net.addLink( glasgow, userOneHost, cls=TCLink , bw=10 )
    net.addLink( hamburg, userTwoHost, cls=TCLink , bw=10 )
    net.addLink( warsaw, userThreeHost, cls=TCLink , bw=10 )
    net.addLink( zurich, userFourHost, cls=TCLink , bw=10 )
    net.addLink( lyon, userFiveHost, cls=TCLink , bw=10 )
    net.addLink( madrid, userSixHost, cls=TCLink , bw=10 )
    net.addLink( amsterdam, userSevenHost, cls=TCLink , bw=10 )

    info( '*** Starting network\n')
    net.build()

    info( '*** Starting switches\n')
    net.get('glasgow').start([c0])
    net.get('dublin').start([c0])
    net.get('london').start([c0])
    net.get('amsterdam').start([c0])
    net.get('brussels').start([c0])
    net.get('paris').start([c0])
    net.get('bordeaux').start([c0])
    net.get('madrid').start([c0])
    net.get('barcelona').start([c0])
    net.get('lyon').start([c0])
    net.get('hamburg').start([c0])
    net.get('frankfurt').start([c0])
    net.get('strasbourg').start([c0])
    net.get('zurich').start([c0])
    net.get('milan').start([c0])
    net.get('oslo').start([c0])
    net.get('copenhagen').start([c0])
    net.get('berlin').start([c0])
    net.get('munich').start([c0])
    net.get('prague').start([c0])
    net.get('vienna').start([c0])
    net.get('zagreb').start([c0])
    net.get('rome').start([c0])
    net.get('stockholm').start([c0])
    net.get('warsaw').start([c0])
    net.get('budapest').start([c0])
    net.get('belgrade').start([c0])
    net.get('athens').start([c0])

    info( '*** Sleep 15 seconds to let controller get topology...\n')
    sleep(15)

    popens = {}

    info( '*** Starting services\n')

    httpLsCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json --logging.file=./http-ls.log --exitStatsFile=./http-ls-exit.xlsx'
    popens[httpLsHost] = httpLsHost.popen(httpLsCommand.split())

    httpLrCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json --logging.file=./http-lr.log --exitStatsFile=./http-lr-exit.xlsx'
    popens[httpLrHost] = httpLrHost.popen(httpLrCommand.split())

    httpSsCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json --logging.file=./http-ss.log --exitStatsFile=./http-ss-exit.xlsx'
    popens[httpSsHost] = httpSsHost.popen(httpSsCommand.split())

    httpSrCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json --logging.file=./http-sr.log --exitStatsFile=./http-sr-exit.xlsx'
    popens[httpSrHost] = httpSrHost.popen(httpSrCommand.split())

    httpSuCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json --logging.file=./http-su.log --exitStatsFile=./http-su-exit.xlsx'
    popens[httpSuHost] = httpSuHost.popen(httpSuCommand.split())

    info( '*** Sleep 30 seconds to let services start...\n')
    sleep(30)

    info( '*** Starting requests generators...\n')

    userOneCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json -lf user-one -st ./user-one-exit.xlsx -er ./user-one-every-request.xlsx -s 11111 -g poisson -l ' + str(lam)
    popens[userOneHost] = userOneHost.popen(userOneCommand.split())

    sleep(1)
    userTwoCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json -lf user-two -st ./user-two-exit.xlsx -er ./user-two-every-request.xlsx -s 22222 -g poisson -l ' + str(lam)
    popens[userTwoHost] = userTwoHost.popen(userTwoCommand.split())

    sleep(1)
    userThreeCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json -lf user-three -st ./user-three-exit.xlsx -er ./user-three-every-request.xlsx -s 33333 -g poisson -l ' + str(lam)
    popens[userThreeHost] = userThreeHost.popen(userThreeCommand.split())

    sleep(1)
    userFourCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json -lf user-four -st ./user-four-exit.xlsx -er ./user-four-every-request.xlsx -s 44444 -g poisson -l ' + str(lam)
    popens[userFourHost] = userFourHost.popen(userFourCommand.split())

    sleep(1)
    userFiveCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json -lf user-five -st ./user-five-exit.xlsx -er ./user-five-every-request.xlsx -s 55555 -g poisson -l ' + str(lam)
    popens[userFiveHost] = userFiveHost.popen(userFiveCommand.split())

    sleep(1)
    userSixCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json -lf user-six -st ./user-six-exit.xlsx -er ./user-six-every-request.xlsx -s 66666 -g poisson -l ' + str(lam)
    popens[userSixHost] = userSixHost.popen(userSixCommand.split())

    sleep(1)
    userSevenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/complex-nobel-eu/mininet/services.json -lf user-seven -st ./user-seven-exit.xlsx -er ./user-seven-every-request.xlsx -s 77777 -g poisson -l ' + str(lam)
    popens[userSevenHost] = userSevenHost.popen(userSevenCommand.split())

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

    complexNobelEu()

