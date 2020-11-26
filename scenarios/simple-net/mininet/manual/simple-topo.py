from mininet.topo import Topo

from mininet.node import OVSKernelSwitch
from mininet.link import TCLink

class SimpleTopo(Topo):

    def __init__( self ):
        "Create simple topo topo."

        # Initialize topology
        Topo.__init__( self )

        # Add switches
        s1 = self.addSwitch( 's1',  cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:01' )
        s2 = self.addSwitch( 's2',  cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:02' )
        s3 = self.addSwitch( 's3',  cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:03' )
        s4 = self.addSwitch( 's4',  cls=OVSKernelSwitch, dpid='00:00:00:00:00:00:00:04' )

        # Add hosts
        hostsNumber = 2
        userOneHost = self.addHost( 'User1',  ip='10.0.0.101/24', cpu=.1/hostsNumber )

        httpLsHost = self.addHost( 'HTTP_LS', ip='10.0.0.1/24', cpu=.1/hostsNumber )
        httpSsHost = self.addHost( 'HTTP_SS', ip='10.0.0.2/24', cpu=.1/hostsNumber )


        # Add links between switches
        self.addLink( s1, s2, cls=TCLink , bw=10 )
        self.addLink( s1, s3, cls=TCLink , bw=10 )

        self.addLink( s2, s4, cls=TCLink , bw=10 )

        self.addLink( s3, s4, cls=TCLink , bw=10 )

        # Add links to services
        self.addLink( s4, httpLsHost, cls=TCLink )
        self.addLink( s4, httpSsHost, cls=TCLink )

        #Add links to users
        self.addLink( s1, userOneHost, cls=TCLink )

topos = { 'simpletopo': (lambda: SimpleTopo())}