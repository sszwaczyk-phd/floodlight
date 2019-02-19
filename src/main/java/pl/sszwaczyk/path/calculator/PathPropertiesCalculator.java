package pl.sszwaczyk.path.calculator;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.routing.Path;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

public abstract class PathPropertiesCalculator {

    protected IOFSwitchService switchService;
    protected ILinkDiscoveryService linkService;

    public PathPropertiesCalculator(IOFSwitchService switchService,
                                    ILinkDiscoveryService linkService) {
        this.switchService = switchService;
        this.linkService = linkService;
    }

    public abstract Map<SecurityDimension, Float> calculatePathProperties(Path path);
}
