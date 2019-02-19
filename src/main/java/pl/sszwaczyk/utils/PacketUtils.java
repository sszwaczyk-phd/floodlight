package pl.sszwaczyk.utils;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;

public class PacketUtils {

    public static AddressAndPort getSrcAddressAndSrcTCPPort(FloodlightContext cntx) {
        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
        if(eth.getEtherType() == EthType.IPv4) {
            IPv4 ipv4 = (IPv4) eth.getPayload();
            if (ipv4.getProtocol() == IpProtocol.TCP) {
                String srcAddr = ipv4.getSourceAddress().toString();
                int srcPort = ((TCP) ipv4.getPayload()).getSourcePort().getPort();
                return new AddressAndPort(srcAddr, srcPort);
            }
        }
        return null;
    }
}
