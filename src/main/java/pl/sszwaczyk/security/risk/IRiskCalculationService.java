package pl.sszwaczyk.security.risk;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

public interface IRiskCalculationService extends IFloodlightService {

    Map<SecurityDimension, Float> calculateRisk(Map<SecurityDimension, Float> securityProperties,
                                                Map<SecurityDimension, Float> consequences);

}
