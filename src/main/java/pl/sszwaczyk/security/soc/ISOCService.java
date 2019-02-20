package pl.sszwaczyk.security.soc;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.threat.Threat;

import java.util.Map;

public interface ISOCService extends IFloodlightService {

    void addListener(ISOCListener listener);

    Map<Threat, Map<SecurityDimension, Float>> getActualThreats();
}
