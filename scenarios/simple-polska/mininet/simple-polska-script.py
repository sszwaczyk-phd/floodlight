#!/usr/bin/python
import os
import signal
import subprocess
import sys
from mininet.link import TCLink
from mininet.log import setLogLevel, info
from mininet.net import Mininet
from mininet.node import Host
from mininet.node import OVSKernelSwitch
from mininet.node import RemoteController
from mininet.util import pmonitor
from signal import SIGTERM
from time import sleep
from time import time


def simplePolska():

    if len(sys.argv) < 2:
        info( '*** Specify time [s] of simulation! --> python simple-polska-script.py <time>...\n')
        return

    simulationTime = int(sys.argv[1])

    info( '*** Starting controller...\n')
    cmd = "java -jar target/floodlight.jar -cf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/floodlightdefault.properties"
    proc = subprocess.Popen(cmd.split(), cwd='/home/sszwaczyk/WAT/PhD/impl/floodlight')

    info( '*** Sleep 5 seconds to let controller start...\n')
    sleep(5)

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/24',
                   autoSetMacs=True,
                   autoStaticArp=True)

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
    userEightHost = net.addHost( 'User8', cls=Host, ip='10.0.0.108/24' )
    userNineHost = net.addHost( 'User9', cls=Host, ip='10.0.0.109/24' )
    userTenHost = net.addHost( 'User10', cls=Host, ip='10.0.0.110/24' )
    userElevenHost = net.addHost( 'User11', cls=Host, ip='10.0.0.111/24' )
    userTwelveHost = net.addHost( 'User12', cls=Host, ip='10.0.0.112/24' )
    userThirteenHost = net.addHost( 'User13', cls=Host, ip='10.0.0.113/24' )
    userFourteenHost = net.addHost( 'User14', cls=Host, ip='10.0.0.114/24' )
    userFifteenHost = net.addHost( 'User15', cls=Host, ip='10.0.0.115/24' )
    userSixteenHost = net.addHost( 'User16', cls=Host, ip='10.0.0.116/24' )
    userSeventeenHost = net.addHost( 'User17', cls=Host, ip='10.0.0.117/24' )
    userEighteenHost = net.addHost( 'User18', cls=Host, ip='10.0.0.118/24' )
    userNineteenHost = net.addHost( 'User19', cls=Host, ip='10.0.0.119/24' )
    userTwentyHost = net.addHost( 'User20', cls=Host, ip='10.0.0.120/24' )
    userTwentyOneHost = net.addHost( 'User21', cls=Host, ip='10.0.0.121/24' )


    serviceOneHost = net.addHost( 'Service1', cls=Host, ip='10.0.0.1/24' )
    serviceTwoHost = net.addHost( 'Service2', cls=Host, ip='10.0.0.2/24' )
    serviceThreeHost = net.addHost( 'Service3', cls=Host, ip='10.0.0.3/24' )
    serviceFourHost = net.addHost( 'Service4', cls=Host, ip='10.0.0.4/24' )
    serviceFiveHost = net.addHost( 'Service5', cls=Host, ip='10.0.0.5/24' )

    info( '*** Add links\n')
    # Add links between switches
    net.addLink( kolobrzeg, gdansk, cls=TCLink , bw=100 )
    net.addLink( kolobrzeg, szczecin, cls=TCLink , bw=100 )
    net.addLink( kolobrzeg, bydgoszcz, cls=TCLink , bw=100 )

    net.addLink( gdansk, bialystok, cls=TCLink , bw=100 )
    net.addLink( gdansk, warsaw, cls=TCLink , bw=100 )

    net.addLink( szczecin, poznan, cls=TCLink , bw=100 )

    net.addLink( bydgoszcz, poznan, cls=TCLink , bw=100 )
    net.addLink( bydgoszcz, warsaw, cls=TCLink , bw=100 )

    net.addLink( bialystok, warsaw, cls=TCLink , bw=100 )
    net.addLink( bialystok, rzeszow, cls=TCLink , bw=100 )

    net.addLink( poznan, wroclaw, cls=TCLink , bw=100 )

    net.addLink( warsaw, lodz, cls=TCLink , bw=100 )
    net.addLink( warsaw, krakow, cls=TCLink , bw=100 )

    net.addLink( lodz, wroclaw, cls=TCLink , bw=100 )
    net.addLink( lodz, katowice, cls=TCLink , bw=100 )

    net.addLink( wroclaw, katowice, cls=TCLink , bw=100 )

    net.addLink( katowice, krakow, cls=TCLink , bw=100 )

    net.addLink( krakow, rzeszow, cls=TCLink , bw=100 )

    # Add links to services
    net.addLink( kolobrzeg, serviceOneHost, cls=TCLink , bw=100 )
    net.addLink( bialystok, serviceTwoHost, cls=TCLink , bw=100 )
    net.addLink( warsaw, serviceThreeHost, cls=TCLink , bw=100 )
    net.addLink( wroclaw, serviceFourHost, cls=TCLink , bw=100 )
    net.addLink( rzeszow, serviceFiveHost, cls=TCLink , bw=100 )

    #Add links to users
    net.addLink( gdansk, userOneHost, cls=TCLink , bw=100 )
    net.addLink( szczecin, userTwoHost, cls=TCLink , bw=100 )
    net.addLink( bydgoszcz, userThreeHost, cls=TCLink , bw=100 )
    net.addLink( poznan, userFourHost, cls=TCLink , bw=100 )
    net.addLink( lodz, userFiveHost, cls=TCLink , bw=100 )
    net.addLink( katowice, userSixHost, cls=TCLink , bw=100 )
    net.addLink( krakow, userSevenHost, cls=TCLink , bw=100 )
    net.addLink( gdansk, userEightHost, cls=TCLink , bw=100 )
    net.addLink( szczecin, userNineHost, cls=TCLink , bw=100 )
    net.addLink( bydgoszcz, userTenHost, cls=TCLink , bw=100 )
    net.addLink( poznan, userElevenHost, cls=TCLink , bw=100 )
    net.addLink( lodz, userTwelveHost, cls=TCLink , bw=100 )
    net.addLink( katowice, userThirteenHost, cls=TCLink , bw=100 )
    net.addLink( krakow, userFourteenHost, cls=TCLink , bw=100 )
    net.addLink( gdansk, userFifteenHost, cls=TCLink , bw=100 )
    net.addLink( szczecin, userSixteenHost, cls=TCLink , bw=100 )
    net.addLink( bydgoszcz, userSeventeenHost, cls=TCLink , bw=100 )
    net.addLink( poznan, userEighteenHost, cls=TCLink , bw=100 )
    net.addLink( lodz, userNineteenHost, cls=TCLink , bw=100 )
    net.addLink( katowice, userTwentyHost, cls=TCLink , bw=100 )
    net.addLink( krakow, userTwentyOneHost, cls=TCLink , bw=100 )

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

    popens = {}
    
    info( '*** Starting services\n')
    
    serviceOneCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-one.log --exitStatsFile=./service-one-exit.xlsx'
    popens[serviceOneHost] = serviceOneHost.popen(serviceOneCommand.split())

    serviceTwoCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-Two.log --exitStatsFile=./service-Two-exit.xlsx'
    popens[serviceTwoHost] = serviceTwoHost.popen(serviceTwoCommand.split())

    serviceThreeCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-Three.log --exitStatsFile=./service-Three-exit.xlsx'
    popens[serviceThreeHost] = serviceThreeHost.popen(serviceThreeCommand.split())

    serviceFourCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-Four.log --exitStatsFile=./service-Four-exit.xlsx'
    popens[serviceFourHost] = serviceFourHost.popen(serviceFourCommand.split())

    serviceFiveCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/http-server/target/http-server-0.0.1-SNAPSHOT.jar --usersFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/users.json --servicesFile=/home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json --logging.file=./service-Five.log --exitStatsFile=./service-Five-exit.xlsx'
    popens[serviceFiveHost] = serviceFiveHost.popen(serviceFiveCommand.split())

    info( '*** Sleep 30 seconds to let services start...\n')
    sleep(30)

    info( '*** Starting requests generators...\n')

    userOneCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-one -st ./user-one-exit.xlsx -er ./user-one-every-request.xlsx -s 11111 -g uniform -ming 30 -maxg 60'
    popens[userOneHost] = userOneHost.popen(userOneCommand.split())

    sleep(1)
    userTwoCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-two -st ./user-two-exit.xlsx -er ./user-two-every-request.xlsx -s 22222 -g uniform -ming 30 -maxg 60'
    popens[userTwoHost] = userTwoHost.popen(userTwoCommand.split())

    sleep(1)
    userThreeCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-three -st ./user-three-exit.xlsx -er ./user-three-every-request.xlsx -s 33333 -g uniform -ming 30 -maxg 60'
    popens[userThreeHost] = userThreeHost.popen(userThreeCommand.split())

    sleep(1)
    userFourCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-four -st ./user-four-exit.xlsx -er ./user-four-every-request.xlsx -s 44444 -g uniform -ming 30 -maxg 60'
    popens[userFourHost] = userFourHost.popen(userFourCommand.split())

    sleep(1)
    userFiveCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-five -st ./user-five-exit.xlsx -er ./user-five-every-request.xlsx -s 55555 -g uniform -ming 30 -maxg 60'
    popens[userFiveHost] = userFiveHost.popen(userFiveCommand.split())

    sleep(1)
    userSixCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-six -st ./user-six-exit.xlsx -er ./user-six-every-request.xlsx -s 66666 -g uniform -ming 30 -maxg 60'
    popens[userSixHost] = userSixHost.popen(userSixCommand.split())

    sleep(1)
    userSevenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-seven -st ./user-seven-exit.xlsx -er ./user-seven-every-request.xlsx -s 77777 -g uniform -ming 30 -maxg 60'
    popens[userSevenHost] = userSevenHost.popen(userSevenCommand.split())

    sleep(1)
    userEightCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-eight -st ./user-eight-exit.xlsx -er ./user-eight-every-request.xlsx -s 88888 -g uniform -ming 30 -maxg 60'
    popens[userEightHost] = userEightHost.popen(userEightCommand.split())

    sleep(1)
    userNineCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Nine -st ./user-Nine-exit.xlsx -er ./user-Nine-every-request.xlsx -s 99999 -g uniform -ming 30 -maxg 60'
    popens[userNineHost] = userNineHost.popen(userNineCommand.split())

    sleep(1)
    userTenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Ten -st ./user-Ten-exit.xlsx -er ./user-Ten-every-request.xlsx -s 1010101010 -g uniform -ming 30 -maxg 60'
    popens[userTenHost] = userTenHost.popen(userTenCommand.split())

    sleep(1)
    userElevenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Eleven -st ./user-Eleven-exit.xlsx -er ./user-Eleven-every-request.xlsx -s 1111111111 -g uniform -ming 30 -maxg 60'
    popens[userElevenHost] = userElevenHost.popen(userElevenCommand.split())

    sleep(1)
    userTwelveCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Twelve -st ./user-Twelve-exit.xlsx -er ./user-Twelve-every-request.xlsx -s 1212121212 -g uniform -ming 30 -maxg 60'
    popens[userTwelveHost] = userTwelveHost.popen(userTwelveCommand.split())

    sleep(1)
    userThirteenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Thirteen -st ./user-Thirteen-exit.xlsx -er ./user-Thirteen-every-request.xlsx -s 1313131313 -g uniform -ming 30 -maxg 60'
    popens[userThirteenHost] = userThirteenHost.popen(userThirteenCommand.split())

    sleep(1)
    userFourteenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Fourteen -st ./user-Fourteen-exit.xlsx -er ./user-Fourteen-every-request.xlsx -s 1414141414 -g uniform -ming 30 -maxg 60'
    popens[userFourteenHost] = userFourteenHost.popen(userFourteenCommand.split())

    sleep(1)
    userFifteenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Fifteen -st ./user-Fifteen-exit.xlsx -er ./user-Fifteen-every-request.xlsx -s 1515151515 -g uniform -ming 30 -maxg 60'
    popens[userFifteenHost] = userFifteenHost.popen(userFifteenCommand.split())

    sleep(1)
    userSixteenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Sixteen -st ./user-Sixteen-exit.xlsx -er ./user-Sixteen-every-request.xlsx -s 1616161616 -g uniform -ming 30 -maxg 60'
    popens[userSixteenHost] = userSixteenHost.popen(userSixteenCommand.split())

    sleep(1)
    userSeventeenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Seventeen -st ./user-Seventeen-exit.xlsx -er ./user-Seventeen-every-request.xlsx -s 1717171717 -g uniform -ming 30 -maxg 60'
    popens[userSeventeenHost] = userSeventeenHost.popen(userSeventeenCommand.split())

    sleep(1)
    userEighteenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Eighteen -st ./user-Eighteen-exit.xlsx -er ./user-Eighteen-every-request.xlsx -s 1818181818 -g uniform -ming 30 -maxg 60'
    popens[userEighteenHost] = userEighteenHost.popen(userEighteenCommand.split())

    sleep(1)
    userNineteenCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Nineteen -st ./user-Nineteen-exit.xlsx -er ./user-Nineteen-every-request.xlsx -s 1919191919 -g uniform -ming 30 -maxg 60'
    popens[userNineteenHost] = userNineteenHost.popen(userNineteenCommand.split())

    sleep(1)
    userTwentyCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-Twenty -st ./user-Twenty-exit.xlsx -er ./user-Twenty-every-request.xlsx -s 2020202020 -g uniform -ming 30 -maxg 60'
    popens[userTwentyHost] = userTwentyHost.popen(userTwentyCommand.split())

    sleep(1)
    userTwentyOneCommand = 'java -jar /home/sszwaczyk/WAT/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/sszwaczyk/WAT/PhD/impl/floodlight/scenarios/simple-polska/mininet/services.json -lf user-TwentyOne -st ./user-TwentyOne-exit.xlsx -er ./user-TwentyOne-every-request.xlsx -s 2121212121 -g uniform -ming 30 -maxg 60'
    popens[userTwentyOneHost] = userTwentyOneHost.popen(userTwentyOneCommand.split())

    # CLI(net)

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

    simplePolska()

