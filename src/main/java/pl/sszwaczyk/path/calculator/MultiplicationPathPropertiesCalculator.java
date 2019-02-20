package pl.sszwaczyk.path.calculator;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplicationPathPropertiesCalculator extends PathPropertiesCalculator {

    public MultiplicationPathPropertiesCalculator(IOFSwitchService switchService,
                                                  ILinkDiscoveryService linkService) {
        super(switchService, linkService);
    }

    @Override
    public Map<SecurityDimension, Float> calculatePathProperties(List<IOFSwitch> switches, List<Link> links) {
        Map<SecurityDimension, Float> pathProperties = new HashMap<>();
        pathProperties.put(SecurityDimension.CONFIDENTIALITY, 1.0f);
        pathProperties.put(SecurityDimension.INTEGRITY, 1.0f);
        pathProperties.put(SecurityDimension.AVAILABILITY, 1.0f);
        pathProperties.put(SecurityDimension.TRUST, 1.0f);

        for(IOFSwitch s: switches) {
            Float switchTrust = (Float) s.getAttributes().get(SecurityDimension.TRUST);
            Float pathTrust = pathProperties.get(SecurityDimension.TRUST);
            pathProperties.put(SecurityDimension.TRUST, pathTrust * switchTrust);
        }

        for(Link l: links) {
            Float linkConfidentiality = l.getConfidentiality();
            Float pathConfidentiality = pathProperties.get(SecurityDimension.CONFIDENTIALITY);
            pathProperties.put(SecurityDimension.CONFIDENTIALITY, pathConfidentiality * linkConfidentiality);

            Float linkIntegrity = l.getIntegrity();
            Float pathIntegrity = pathProperties.get(SecurityDimension.INTEGRITY);
            pathProperties.put(SecurityDimension.INTEGRITY, pathIntegrity * linkIntegrity);

            Float linkAvailability = l.getAvailability();
            Float pathAvailability = pathProperties.get(SecurityDimension.AVAILABILITY);
            pathProperties.put(SecurityDimension.AVAILABILITY, pathAvailability * linkAvailability);
        }

        return pathProperties;
    }

}
