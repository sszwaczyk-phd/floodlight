package pl.sszwaczyk.security.soc.calculator;

import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.threat.Threat;

import java.util.Map;

public interface ThreatInfluenceCalculator {

    Map<SecurityDimension, Float> calculateThreatInfluence(Threat threat);

}
