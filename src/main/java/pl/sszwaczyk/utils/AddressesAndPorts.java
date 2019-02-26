package pl.sszwaczyk.utils;

import lombok.Data;
import net.floodlightcontroller.core.FloodlightContext;

@Data
public class AddressesAndPorts {

    private AddressAndPort src;
    private AddressAndPort dst;

    public static AddressesAndPorts fromCntx(FloodlightContext cntx) {
        AddressesAndPorts addressesAndPorts = new AddressesAndPorts();
        addressesAndPorts.setSrc(PacketUtils.getSrcAddressAndSrcTCPPort(cntx));
        addressesAndPorts.setDst(PacketUtils.getDstAddressAndDstTCPPort(cntx));
        return addressesAndPorts;
    }

}
