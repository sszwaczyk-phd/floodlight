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
public class RandomSameValueThreatInfluenceCalculator implements ThreatInfluenceCalculator {

    private long seed;

    private double min;
    private double max;

    private boolean influenceT;
    private boolean influenceC;
    private boolean influenceI;
    private boolean influenceA;

    private Random random;

    @Override
    public Map<SecurityDimension, Float> calculateThreatInfluence(Threat threat) {
        if(random == null) {
            random = new Random(seed);
        }

        Map<SecurityDimension, Float> influence = new HashMap<>();

        double randomInfluence = min + (max - min) * random.nextDouble();

        if(influenceT) {
            influence.put(SecurityDimension.TRUST, (float) randomInfluence);
        } else {
            influence.put(SecurityDimension.TRUST, 0.0f);
        }

        if(influenceC) {
            influence.put(SecurityDimension.CONFIDENTIALITY, (float) randomInfluence);
        } else {
            influence.put(SecurityDimension.CONFIDENTIALITY, 0.0f);
        }

        if(influenceI) {
            influence.put(SecurityDimension.INTEGRITY, (float) randomInfluence);
        } else {
            influence.put(SecurityDimension.INTEGRITY, 0.0f);
        }

        if(influenceA) {
            influence.put(SecurityDimension.AVAILABILITY, (float) randomInfluence);
        } else {
            influence.put(SecurityDimension.AVAILABILITY, 0.0f);
        }

        return influence;

    }

}
