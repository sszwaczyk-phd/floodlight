package pl.sszwaczyk.security.risk.calculator;

import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

public interface RiskCalculator {

    Map<SecurityDimension, Float> calculateRisk(Map<SecurityDimension, Float> securityProperties,
                                                Map<SecurityDimension, Float> consequences);

}
