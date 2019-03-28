package pl.sszwaczyk.statistics;

import lombok.Data;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.routing.solver.SolveRegion;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.threat.Threat;
import pl.sszwaczyk.security.threat.ThreatWithInfluence;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SecureRoutingStatistics {

    private List<Decision> decisions = new ArrayList<>();

    private List<ThreatWithInfluence> threats = new ArrayList<>();

    List<ServerResponse> realizedList = new ArrayList<>();

    private int total;

    private int realized;
    private int realizedInRarBf;
    private int realizedInRarRf;

    private int notRealized;

    private Map<User, Map<Service, RelationStats>> relationStatsMap = new HashMap();

    public void addDecision(Decision decision) {
        decisions.add(decision);
    }

    public void addThreat(Threat threat, Map<SecurityDimension, Float> influence) {
        ThreatWithInfluence threatWithInfluence = ThreatWithInfluence.builder()
                .threat(threat)
                .influence(influence)
                .build();
        threats.add(threatWithInfluence);
    }
}
