package pl.sszwaczyk.security.dtsp;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.service.Service;

public interface IDTSPService extends IFloodlightService {

    DTSP getDTSPForService(Service service);

}
