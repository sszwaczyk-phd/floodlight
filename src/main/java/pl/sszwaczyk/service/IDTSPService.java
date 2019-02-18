package pl.sszwaczyk.service;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.domain.security.DTSP;
import pl.sszwaczyk.domain.Service;

public interface IDTSPService extends IFloodlightService {

    DTSP getDTSPForService(Service service);

}
