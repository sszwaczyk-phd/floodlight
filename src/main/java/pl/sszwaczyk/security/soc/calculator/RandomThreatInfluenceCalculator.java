package pl.sszwaczyk.security.soc.calculator;

import lombok.Builder;
import lombok.Data;
import org.projectfloodlight.openflow.types.DatapathId;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.threat.Threat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Data
@Builder
public class RandomThreatInfluenceCalculator implements ThreatInfluenceCalculator {

    private double min;
    private double max;

    @Override
    public Map<SecurityDimension, Float> calculateThreatInfluence(Threat threat) {
        Map<SecurityDimension, Float> influence = new HashMap<>();
        DatapathId dst = threat.getDst();
        if(dst == null) {
            double trustInfluence = ThreadLocalRandom.current().nextDouble(min, max);
            influence.put(SecurityDimension.TRUST, (float) trustInfluence);
        } else {
            double cInfluence = ThreadLocalRandom.current().nextDouble(min, max);
            double iInfluence = ThreadLocalRandom.current().nextDouble(min, max);
            double aInfluence = ThreadLocalRandom.current().nextDouble(min, max);
            influence.put(SecurityDimension.CONFIDENTIALITY, (float) cInfluence);
            influence.put(SecurityDimension.INTEGRITY, (float) iInfluence);
            influence.put(SecurityDimension.AVAILABILITY, (float) aInfluence);
        }
        return influence;
    }
}
