from mininet.topo import Topo

class SimpleTopo(Topo):

    def __init__( self ):
        "Create simple topo topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        userHost = self.addHost( 'User1' )
        serviceHost = self.addHost( 'Service1' )
        leftSwitch = self.addSwitch( 's1' )
        upSwitch = self.addSwitch( 's2' )
        downSwitch = self.addSwitch( 's3' )
        rightSwitch = self.addSwitch( 's4' )

        # Add links
        self.addLink( userHost, leftSwitch )
        self.addLink( leftSwitch, upSwitch )
        self.addLink( leftSwitch, downSwitch )
        self.addLink( upSwitch, rightSwitch )
        self.addLink( downSwitch, rightSwitch )
        self.addLink( rightSwitch, serviceHost )

topos = { 'simpletopo': (lambda: SimpleTopo())}
