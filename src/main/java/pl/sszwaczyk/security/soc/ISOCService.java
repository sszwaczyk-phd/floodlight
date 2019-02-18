package pl.sszwaczyk.security.soc;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface ISOCService extends IFloodlightService {

    void addListener(ISOCListener listener);

}
