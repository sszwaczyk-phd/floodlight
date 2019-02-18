package pl.sszwaczyk.service;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.domain.SecurityDimension;

import java.util.Map;

public interface IRiskCalculationService extends IFloodlightService {

    Map<SecurityDimension, Float> calculateRisk(Map<SecurityDimension, Float> securityProperties,
                                                Map<SecurityDimension, Float> consequences);

}
