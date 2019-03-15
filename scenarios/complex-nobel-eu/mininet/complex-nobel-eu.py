from mininet.topo import Topo

class ComplexNobelEu(Topo):

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
        userEightHost = self.addHost( 'User8', ip='10.0.0.108/24' )
        userNineHost = self.addHost( 'User9', ip='10.0.0.109/24' )
        userTenHost = self.addHost( 'User10', ip='10.0.0.110/24' )

        serviceOneHost = self.addHost( 'Service1', ip='10.0.0.1/24' )
        serviceTwoHost = self.addHost( 'Service2', ip='10.0.0.2/24' )
        serviceThreeHost = self.addHost( 'Service3', ip='10.0.0.3/24' )
        serviceFourHost = self.addHost( 'Service4', ip='10.0.0.4/24' )
        serviceFiveHost = self.addHost( 'Service5', ip='10.0.0.5/24' )

        glasgow = self.addSwitch( 'glasgow', dpid='00:00:00:00:00:00:00:01' )
        dublin = self.addSwitch( 'dublin', dpid='00:00:00:00:00:00:00:02' )
        london = self.addSwitch( 'london', dpid='00:00:00:00:00:00:00:03' )
        amsterdam = self.addSwitch( 'amsterdam', dpid='00:00:00:00:00:00:00:04' )
        brussels = self.addSwitch( 'brussels', dpid='00:00:00:00:00:00:00:05' )
        paris = self.addSwitch( 'paris', dpid='00:00:00:00:00:00:00:06' )
        bordeaux = self.addSwitch( 'bordeaux', dpid='00:00:00:00:00:00:00:07' )
        madrid = self.addSwitch( 'madrid', dpid='00:00:00:00:00:00:00:08' )
        barcelona = self.addSwitch( 'barcelona', dpid='00:00:00:00:00:00:00:09' )
        lyon = self.addSwitch( 'lyon', dpid='00:00:00:00:00:00:00:10' )
        hamburg = self.addSwitch( 'hamburg', dpid='00:00:00:00:00:00:00:11' )
        frankfurt = self.addSwitch( 'frankfurt', dpid='00:00:00:00:00:00:00:12' )
        strasbourg = self.addSwitch( 'strasbourg', dpid='00:00:00:00:00:00:00:13' )
        zurich = self.addSwitch( 'zurich', dpid='00:00:00:00:00:00:00:14' )
        milan = self.addSwitch( 'milan', dpid='00:00:00:00:00:00:00:15' )
        oslo = self.addSwitch( 'oslo', dpid='00:00:00:00:00:00:00:16' )
        copenhagen = self.addSwitch( 'copenhagen', dpid='00:00:00:00:00:00:00:17' )
        berlin = self.addSwitch( 'berlin', dpid='00:00:00:00:00:00:00:18' )
        munich = self.addSwitch( 'munich', dpid='00:00:00:00:00:00:00:19' )
        prague = self.addSwitch( 'prague', dpid='00:00:00:00:00:00:00:20' )
        vienna = self.addSwitch( 'vienna', dpid='00:00:00:00:00:00:00:21' )
        zagreb = self.addSwitch( 'zagreb', dpid='00:00:00:00:00:00:00:22' )
        rome = self.addSwitch( 'rome', dpid='00:00:00:00:00:00:00:23' )
        stockholm = self.addSwitch( 'stockholm', dpid='00:00:00:00:00:00:00:24' )
        warsaw = self.addSwitch( 'warsaw', dpid='00:00:00:00:00:00:00:25' )
        budapest = self.addSwitch( 'budapest', dpid='00:00:00:00:00:00:00:26' )
        belgrade = self.addSwitch( 'belgrade', dpid='00:00:00:00:00:00:00:27' )
        athens = self.addSwitch( 'athens', dpid='00:00:00:00:00:00:00:28' )

        # Add links between switches
        self.addLink( glasgow, dublin, bw=10 )
        self.addLink( glasgow, amsterdam, bw=10 )

        self.addLink( dublin, london, bw=10 )

        self.addLink( london, amsterdam, bw=10 )
        self.addLink( london, paris, bw=10 )

        self.addLink( paris, brussels, bw=10 )
        self.addLink( paris, bordeaux, bw=10 )
        self.addLink( paris, strasbourg, bw=10 )
        self.addLink( paris, lyon, bw=10 )

        self.addLink( bordeaux, madrid, bw=10 )

        self.addLink( madrid, barcelona, bw=10 )

        self.addLink( barcelona, lyon, bw=10 )

        self.addLink( lyon, zurich, bw=10 )

        self.addLink( amsterdam, brussels, bw=10 )
        self.addLink( amsterdam, hamburg, bw=10 )

        self.addLink( brussels, frankfurt, bw=10 )

        self.addLink( hamburg, frankfurt, bw=10 )
        self.addLink( hamburg, berlin, bw=10 )

        self.addLink( frankfurt, strasbourg, bw=10 )
        self.addLink( frankfurt, munich, bw=10 )
        self.addLink( frankfurt, strasbourg, bw=10 )

        self.addLink( strasbourg, zurich, bw=10 )

        self.addLink( zurich, milan, bw=10 )

        self.addLink( milan, munich, bw=10 )
        self.addLink( milan, rome, bw=10 )

        self.addLink( oslo, stockholm, bw=10 )
        self.addLink( oslo, copenhagen, bw=10 )

        self.addLink( copenhagen, berlin, bw=10 )

        self.addLink( berlin, warsaw, bw=10 )
        self.addLink( berlin, prague, bw=10 )
        self.addLink( berlin, munich, bw=10 )

        self.addLink( munich, vienna, bw=10 )

        self.addLink( prague, vienna, bw=10 )
        self.addLink( prague, budapest, bw=10 )

        self.addLink( vienna, zagreb, bw=10 )

        self.addLink( zagreb, rome, bw=10 )

        self.addLink( rome, athens, bw=10 )

        self.addLink( stockholm, warsaw, bw=10 )

        self.addLink( warsaw, budapest, bw=10 )

        self.addLink( budapest, belgrade, bw=10 )

        self.addLink( belgrade, athens, bw=10 )

        # Add links to services
        self.addLink( barcelona, serviceOneHost, bw=10 )
        self.addLink( budapest, serviceTwoHost, bw=10 )
        self.addLink( london, serviceThreeHost, bw=10 )
        self.addLink( munich, serviceFourHost, bw=10 )
        self.addLink( hamburg, serviceFiveHost, bw=10 )

        #Add links to users
        self.addLink( rome, userOneHost, bw=10 )
        self.addLink( dublin, userTwoHost, bw=10 )
        self.addLink( amsterdam, userThreeHost, bw=10 )
        self.addLink( frankfurt, userFourHost, bw=10 )
        self.addLink( prague, userFiveHost, bw=10 )
        self.addLink( milan, userSixHost, bw=10 )
        self.addLink( athens, userSevenHost, bw=10 )
        self.addLink( bordeaux, userEightHost, bw=10 )
        self.addLink( stockholm, userNineHost, bw=10 )
        self.addLink( strasbourg, userTenHost, bw=10 )

topos = { 'complexnobeleu': (lambda: ComplexNobelEu())}
