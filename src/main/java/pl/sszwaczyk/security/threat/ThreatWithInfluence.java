package pl.sszwaczyk.security.threat;

import lombok.Builder;
import lombok.Data;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

@Builder
@Data
public class ThreatWithInfluence {
    private Threat threat;
    private Map<SecurityDimension, Float> influence;
}
