#!/usr/bin/python

import os
import signal
import subprocess
from mininet.link import TCLink
from mininet.log import setLogLevel, info
from mininet.net import Mininet
from mininet.node import Host
from mininet.node import OVSKernelSwitch
from mininet.node import RemoteController
from time import sleep


def simplePolska():

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/24')

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0',
                         controller=RemoteController,
                         ip='127.0.0.1',
                         protocol='tcp',
                         port=6653)

    info( '*** Add switches\n')
    kolobrzeg = net.addSwitch( 'kolobrzeg', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:01' )
    gdansk = net.addSwitch( 'gdansk', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:02' )
    szczecin = net.addSwitch( 'szczecin', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:03' )
    bydgoszcz = net.addSwitch( 'bydgoszcz', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:04' )
    bialystok = net.addSwitch( 'bialystok', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:05' )
    poznan = net.addSwitch( 'poznan', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:06' )
    warsaw = net.addSwitch( 'warsaw', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:07' )
    lodz = net.addSwitch( 'lodz', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:08' )
    wroclaw = net.addSwitch( 'wroclaw', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:09' )
    katowice = net.addSwitch( 'katowice', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:10' )
    krakow = net.addSwitch( 'krakow', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:11' )
    rzeszow = net.addSwitch( 'rzeszow', cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:12' )

    info( '*** Add hosts\n')
    userOneHost = net.addHost( 'User1', cls=Host, ip='10.0.0.101/24' )
    userTwoHost = net.addHost( 'User2', cls=Host, ip='10.0.0.102/24' )
    userThreeHost = net.addHost( 'User3', cls=Host, ip='10.0.0.103/24' )
    userFourHost = net.addHost( 'User4', cls=Host, ip='10.0.0.104/24' )
    userFiveHost = net.addHost( 'User5', cls=Host, ip='10.0.0.105/24' )
    userSixHost = net.addHost( 'User6', cls=Host, ip='10.0.0.106/24' )
    userSevenHost = net.addHost( 'User7', cls=Host, ip='10.0.0.107/24' )

    serviceOneHost = net.addHost( 'Service1', cls=Host, ip='10.0.0.1/24' )
    serviceTwoHost = net.addHost( 'Service2', cls=Host, ip='10.0.0.2/24' )
    serviceThreeHost = net.addHost( 'Service3', cls=Host, ip='10.0.0.3/24' )
    serviceFourHost = net.addHost( 'Service4', cls=Host, ip='10.0.0.4/24' )
    serviceFiveHost = net.addHost( 'Service5', cls=Host, ip='10.0.0.5/24' )

    info( '*** Add links\n')
    # Add links between switches
    net.addLink( kolobrzeg, gdansk, cls=TCLink , bw=10 )
    net.addLink( kolobrzeg, szczecin, cls=TCLink , bw=10 )
    net.addLink( kolobrzeg, bydgoszcz, cls=TCLink , bw=10 )

    net.addLink( gdansk, bialystok, cls=TCLink , bw=10 )
    net.addLink( gdansk, warsaw, cls=TCLink , bw=10 )

    net.addLink( szczecin, poznan, cls=TCLink , bw=10 )

    net.addLink( bydgoszcz, poznan, cls=TCLink , bw=10 )
    net.addLink( bydgoszcz, warsaw, cls=TCLink , bw=10 )

    net.addLink( bialystok, warsaw, cls=TCLink , bw=10 )
    net.addLink( bialystok, rzeszow, cls=TCLink , bw=10 )

    net.addLink( poznan, wroclaw, cls=TCLink , bw=10 )

    net.addLink( warsaw, lodz, cls=TCLink , bw=10 )
    net.addLink( warsaw, krakow, cls=TCLink , bw=10 )

    net.addLink( lodz, wroclaw, cls=TCLink , bw=10 )
    net.addLink( lodz, katowice, cls=TCLink , bw=10 )

    net.addLink( wroclaw, katowice, cls=TCLink , bw=10 )

    net.addLink( katowice, krakow, cls=TCLink , bw=10 )

    net.addLink( krakow, rzeszow, cls=TCLink , bw=10 )

    # Add links to services
    net.addLink( kolobrzeg, serviceOneHost, cls=TCLink , bw=10 )
    net.addLink( bialystok, serviceTwoHost, cls=TCLink , bw=10 )
    net.addLink( warsaw, serviceThreeHost, cls=TCLink , bw=10 )
    net.addLink( wroclaw, serviceFourHost, cls=TCLink , bw=10 )
    net.addLink( rzeszow, serviceFiveHost, cls=TCLink , bw=10 )

    #Add links to users
    net.addLink( gdansk, userOneHost, cls=TCLink , bw=10 )
    net.addLink( szczecin, userTwoHost, cls=TCLink , bw=10 )
    net.addLink( bydgoszcz, userThreeHost, cls=TCLink , bw=10 )
    net.addLink( poznan, userFourHost, cls=TCLink , bw=10 )
    net.addLink( lodz, userFiveHost, cls=TCLink , bw=10 )
    net.addLink( katowice, userSixHost, cls=TCLink , bw=10 )
    net.addLink( krakow, userSevenHost, cls=TCLink , bw=10 )


    info( '*** Starting network\n')
    net.build()

    info( '*** Starting switches\n')
    net.get('kolobrzeg').start([c0])
    net.get('gdansk').start([c0])
    net.get('szczecin').start([c0])
    net.get('bydgoszcz').start([c0])
    net.get('bialystok').start([c0])
    net.get('poznan').start([c0])
    net.get('warsaw').start([c0])
    net.get('lodz').start([c0])
    net.get('wroclaw').start([c0])
    net.get('katowice').start([c0])
    net.get('krakow').start([c0])
    net.get('rzeszow').start([c0])

    info( '*** Sleep 15 seconds to let controller get topology...\n')
    sleep(15)

    info( '*** Starting services\n')
    pidString = serviceOneHost.cmdPrint('java -jar /impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-one.log --exitStatsFile=./service-one-exit.xlsx &')
    serviceOnePid = int( pidString.split(" ")[1] )
    pidString = serviceTwoHost.cmdPrint('java -jar /impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-two.log --exitStatsFile=./service-two-exit.xlsx &')
    serviceTwoPid = int( pidString.split(" ")[1] )
    pidString = serviceThreeHost.cmdPrint('java -jar /impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-three.log --exitStatsFile=./service-three-exit.xlsx &')
    serviceThreePid = int( pidString.split(" ")[1] )
    pidString = serviceFourHost.cmdPrint('java -jar /impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-four.log --exitStatsFile=./service-four-exit.xlsx &')
    serviceFourPid = int( pidString.split(" ")[1] )
    pidString = serviceFiveHost.cmdPrint('java -jar /impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-five.log --exitStatsFile=./service-five-exit.xlsx &')
    serviceFivePid = int( pidString.split(" ")[1] )

    info( '*** Sleep 15 seconds to let services start...\n')
    sleep(15)

    info( '*** Starting requests generators...\n')
    pidString = userOneHost.cmdPrint('java -jar /impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-one -st ./user-one-exit.xlsx -er ./user-one-every-request.xlsx -g uniform -ming 5 -maxg 10 &')
    userOnePid = int( pidString.split(" ")[1] )
    pidString = userTwoHost.cmdPrint('java -jar /impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-two -st ./user-two-exit.xlsx -er ./user-two-every-request.xlsx -g uniform -ming 5 -maxg 10 &')
    userTwoPid = int( pidString.split(" ")[1] )
    pidString = userThreeHost.cmdPrint('java -jar /impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-three -st ./user-three-exit.xlsx -er ./user-three-every-request.xlsx -g uniform -ming 5 -maxg 10 &')
    userThreePid = int( pidString.split(" ")[1] )
    pidString = userFourHost.cmdPrint('java -jar /impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-four -st ./user-four-exit.xlsx -er ./user-four-every-request.xlsx -g uniform -ming 5 -maxg 10 &')
    userFourPid = int( pidString.split(" ")[1] )
    pidString = userFiveHost.cmdPrint('java -jar /impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-five -st ./user-five-exit.xlsx -er ./user-five-every-request.xlsx -g uniform -ming 5 -maxg 10 &')
    userFivePid = int( pidString.split(" ")[1] )
    pidString = userSixHost.cmdPrint('java -jar /impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-six -st ./user-six-exit.xlsx -er ./user-six-every-request.xlsx -g uniform -ming 5 -maxg 10 &')
    userSixPid = int( pidString.split(" ")[1] )
    pidString = userSevenHost.cmdPrint('java -jar /impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-seven -st ./user-seven-exit.xlsx -er ./user-seven-every-request.xlsx -g uniform -ming 5 -maxg 10 &')
    userSevenPid = int( pidString.split(" ")[1] )

    info( '*** Simulation...\n')
    sleep(60)

    info( '*** Stopping requests generators...\n')
    userOneHost.cmd('kill', userOnePid)
    userTwoHost.cmd('kill', userTwoPid)
    userThreeHost.cmd('kill', userThreePid)
    userFourHost.cmd('kill', userFourPid)
    userFiveHost.cmd('kill', userFivePid)
    userSixHost.cmd('kill', userSixPid)
    userSevenHost.cmd('kill', userSevenPid)

    info( '*** Stopping services...\n')
    serviceOneHost.cmd('kill', serviceOnePid)
    serviceTwoHost.cmd('kill', serviceTwoPid)
    serviceThreeHost.cmd('kill', serviceThreePid)
    serviceFourHost.cmd('kill', serviceFourPid)
    serviceFiveHost.cmd('kill', serviceFivePid)

    info( '*** Stopping net...\n')
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )

    info( '*** Starting controller...\n')
    cmd = "java -jar target/floodlight.jar -cf /impl/floodlight/scenarios/simple-polska/mininet/floodlightdefault.properties"
    proc = subprocess.Popen(cmd.split(), cwd='/impl/floodlight')

    info( '*** Sleep 15 seconds to let controller start...\n')
    sleep(15)

    simplePolska()

    info( '*** Stopping controller...\n')
    os.kill(proc.pid, signal.SIGTERM)