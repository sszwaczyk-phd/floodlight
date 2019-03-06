package pl.sszwaczyk.utils;

import lombok.Builder;
import lombok.Data;
import net.floodlightcontroller.core.FloodlightContext;

@Data
@Builder
public class AddressesAndPorts {

    private AddressAndPort src;
    private AddressAndPort dst;

    public static AddressesAndPorts fromCntx(FloodlightContext cntx) {
        AddressesAndPorts addressesAndPorts = AddressesAndPorts.builder().build();
        addressesAndPorts.setSrc(PacketUtils.getSrcAddressAndSrcTCPPort(cntx));
        addressesAndPorts.setDst(PacketUtils.getDstAddressAndDstTCPPort(cntx));
        return addressesAndPorts;
    }

}
