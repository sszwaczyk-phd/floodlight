package pl.sszwaczyk.security.soc.calculator;

import lombok.Builder;
import lombok.Data;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.threat.Threat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Data
@Builder
public class RandomThreatInfluenceCalculator implements ThreatInfluenceCalculator {

    private long seed;

    private double minT;
    private double maxT;

    private double minC;
    private double maxC;

    private double minI;
    private double maxI;

    private double minA;
    private double maxA;

    private Random random;

    @Override
    public Map<SecurityDimension, Float> calculateThreatInfluence(Threat threat) {
        if(random == null) {
            random = new Random(seed);
        }

        Map<SecurityDimension, Float> influence = new HashMap<>();

        if(maxT != 0 ) {
            double trustInfluence = minT + (maxT - minT) * random.nextDouble();
            influence.put(SecurityDimension.TRUST, (float) trustInfluence);
        } else {
            influence.put(SecurityDimension.TRUST, 0.0f);
        }

        if(maxC != 0) {
            double cInfluence = minC + (maxC - minC) * random.nextDouble();
            influence.put(SecurityDimension.CONFIDENTIALITY, (float) cInfluence);
        } else {
            influence.put(SecurityDimension.CONFIDENTIALITY, 0.0f);
        }

        if(maxI != 0) {
            double iInfluence = minI + (maxI - minI) * random.nextDouble();
            influence.put(SecurityDimension.INTEGRITY, (float) iInfluence);
        } else {
            influence.put(SecurityDimension.INTEGRITY, 0.0f);
        }

        if(maxA != 0) {
            double aInfluence = minA + (maxA - minA) * random.nextDouble();
            influence.put(SecurityDimension.AVAILABILITY, (float) aInfluence);
        } else {
            influence.put(SecurityDimension.AVAILABILITY, 0.0f);
        }

        return influence;
    }
}
