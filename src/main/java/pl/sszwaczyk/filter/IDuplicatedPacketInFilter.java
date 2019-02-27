package pl.sszwaczyk.filter;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.utils.AddressesAndPorts;

public interface IDuplicatedPacketInFilter extends IFloodlightService {

    void deleteFromBuffering(AddressesAndPorts ap);

}
