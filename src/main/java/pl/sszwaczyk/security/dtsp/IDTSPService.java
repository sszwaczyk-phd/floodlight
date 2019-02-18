package pl.sszwaczyk.security.dtsp;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.service.Service;

import java.util.List;

public interface IDTSPService extends IFloodlightService {

    List<DTSP> getAllDTSPs();

    DTSP getDTSPForService(Service service);

}
