from mininet.topo import Topo

class SimplePolska(Topo):

    def __init__( self ):
        "Create simple topo topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        userOneHost = self.addHost( 'User1', ip='10.0.0.101/24' )
        userTwoHost = self.addHost( 'User2', ip='10.0.0.102/24' )
        userThreeHost = self.addHost( 'User3', ip='10.0.0.103/24' )
        userFourHost = self.addHost( 'User4', ip='10.0.0.104/24' )
        userFiveHost = self.addHost( 'User5', ip='10.0.0.105/24' )
        userSixHost = self.addHost( 'User6', ip='10.0.0.106/24' )
        userSevenHost = self.addHost( 'User7', ip='10.0.0.107/24' )

        serviceOneHost = self.addHost( 'Service1', ip='10.0.0.1/24' )
        serviceTwoHost = self.addHost( 'Service2', ip='10.0.0.2/24' )
        serviceThreeHost = self.addHost( 'Service3', ip='10.0.0.3/24' )
        serviceFourHost = self.addHost( 'Service4', ip='10.0.0.4/24' )
        serviceFiveHost = self.addHost( 'Service5', ip='10.0.0.5/24' )

        kolobrzeg = self.addSwitch( 'kolobrzeg', dpid='00:00:00:00:00:00:00:01' )
        gdansk = self.addSwitch( 'gdansk', dpid='00:00:00:00:00:00:00:02' )
        szczecin = self.addSwitch( 'szczecin', dpid='00:00:00:00:00:00:00:03' )
        bydgoszcz = self.addSwitch( 'bydgoszcz', dpid='00:00:00:00:00:00:00:04' )
        bialystok = self.addSwitch( 'bialystok', dpid='00:00:00:00:00:00:00:05' )
        poznan = self.addSwitch( 'poznan', dpid='00:00:00:00:00:00:00:06' )
        warsaw = self.addSwitch( 'warsaw', dpid='00:00:00:00:00:00:00:07' )
        lodz = self.addSwitch( 'lodz', dpid='00:00:00:00:00:00:00:08' )
        wroclaw = self.addSwitch( 'wroclaw', dpid='00:00:00:00:00:00:00:09' )
        katowice = self.addSwitch( 'katowice', dpid='00:00:00:00:00:00:00:10' )
        krakow = self.addSwitch( 'krakow', dpid='00:00:00:00:00:00:00:11' )
        rzeszow = self.addSwitch( 'rzeszow', dpid='00:00:00:00:00:00:00:12' )

        # Add links between switches
        self.addLink( kolobrzeg, gdansk, bw=10 )
        self.addLink( kolobrzeg, szczecin, bw=10 )
        self.addLink( kolobrzeg, bydgoszcz, bw=10 )

        self.addLink( gdansk, bialystok, bw=10 )
        self.addLink( gdansk, warsaw, bw=10 )

        self.addLink( szczecin, poznan, bw=10 )

        self.addLink( bydgoszcz, poznan, bw=10 )
        self.addLink( bydgoszcz, warsaw, bw=10 )

        self.addLink( bialystok, warsaw, bw=10 )
        self.addLink( bialystok, rzeszow, bw=10 )

        self.addLink( poznan, wroclaw, bw=10 )

        self.addLink( warsaw, lodz, bw=10 )
        self.addLink( warsaw, krakow, bw=10 )

        self.addLink( lodz, wroclaw, bw=10 )
        self.addLink( lodz, katowice, bw=10 )

        self.addLink( wroclaw, katowice, bw=10 )

        self.addLink( katowice, krakow, bw=10 )

        self.addLink( krakow, rzeszow, bw=10 )

        # Add links to services
        self.addLink( kolobrzeg, serviceOneHost, bw=10 )
        self.addLink( bialystok, serviceTwoHost, bw=10 )
        self.addLink( warsaw, serviceThreeHost, bw=10 )
        self.addLink( wroclaw, serviceFourHost, bw=10 )
        self.addLink( rzeszow, serviceFiveHost, bw=10 )

        #Add links to users
        self.addLink( gdansk, userOneHost, bw=10 )
        self.addLink( szczecin, userTwoHost, bw=10 )
        self.addLink( bydgoszcz, userThreeHost, bw=10 )
        self.addLink( poznan, userFourHost, bw=10 )
        self.addLink( lodz, userFiveHost, bw=10 )
        self.addLink( katowice, userSixHost, bw=10 )
        self.addLink( krakow, userSevenHost, bw=10 )

topos = { 'simplepolska': (lambda: SimplePolska())}
