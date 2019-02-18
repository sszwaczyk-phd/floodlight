package pl.sszwaczyk.security.threat;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface IThreatService extends IFloodlightService {

    void addListener(IThreatListener listener);

}
