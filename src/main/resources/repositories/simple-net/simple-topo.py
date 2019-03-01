from mininet.topo import Topo

class SimpleTopo(Topo):

    def __init__( self ):
        "Create simple topo topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        userOneHost = self.addHost( 'User1', ip='10.0.0.1/24' )
        userTwoHost = self.addHost( 'User2', ip='10.0.0.2/24' )
        serviceOneHost = self.addHost( 'Service1', ip='10.0.0.100/24' )
        leftSwitch = self.addSwitch( 's1' )
        upSwitch = self.addSwitch( 's2' )
        downSwitch = self.addSwitch( 's3' )
        rightSwitch = self.addSwitch( 's4' )

        # Add links
        self.addLink( userOneHost, leftSwitch, bw=10 )
        self.addLink( userTwoHost, upSwitch, bw=10 )
        self.addLink( leftSwitch, upSwitch, bw=10 )
        self.addLink( leftSwitch, downSwitch, bw=10 )
        self.addLink( upSwitch, rightSwitch, bw=10 )
        self.addLink( downSwitch, rightSwitch, bw=10 )
        self.addLink( rightSwitch, serviceOneHost, bw=10 )

topos = { 'simpletopo': (lambda: SimpleTopo())}