package pl.sszwaczyk.domain.security.risk;

import pl.sszwaczyk.domain.SecurityDimension;

import java.util.Map;

public interface RiskCalculator {

    Map<SecurityDimension, Float> calculateRisk(Map<SecurityDimension, Float> securityProperties,
                                                Map<SecurityDimension, Float> consequences);

}
