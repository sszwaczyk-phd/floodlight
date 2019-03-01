package pl.sszwaczyk.statistics;

import lombok.Data;
import pl.sszwaczyk.routing.solver.SolveRegion;
import pl.sszwaczyk.routing.solver.SolveResult;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SecureRoutingStatistics {

    List<ServerResponse> realizedList = new ArrayList<>();

    private int total;

    private int realized;
    private int realizedInRarBf;
    private int realizedInRarRf;

    private int notRealized;

    private Map<User, Map<Service, RelationStats>> relationStatsMap = new HashMap();

    public void updateRealized(User user, Service service, SolveResult result) {
        total++;
        realized++;

        if(result.getRegion().equals(SolveRegion.RAR_BF)) {
            realizedInRarBf++;
        } else {
            realizedInRarRf++;
        }

        Map<Service, RelationStats> serviceRelationStatsMap = relationStatsMap.get(user);
        if(serviceRelationStatsMap == null) {
            serviceRelationStatsMap = new HashMap<>();
            RelationStats relationStats = new RelationStats();
            relationStats.updateRealized(result.getRegion());
            serviceRelationStatsMap.put(service, relationStats);
            relationStatsMap.put(user, serviceRelationStatsMap);
        } else {
            RelationStats relationStats = serviceRelationStatsMap.get(service);
            if(relationStats == null) {
                relationStats = new RelationStats();
                relationStats.updateRealized(result.getRegion());
                serviceRelationStatsMap.put(service, relationStats);
            } else {
                relationStats.updateRealized(result.getRegion());
            }

        }

        ServerResponse serverResponse = ServerResponse.builder()
                .time(LocalTime.now())
                .serviceId(service.getId())
                .userId(user.getId())
                .solveResult(result)
                .build();
        realizedList.add(serverResponse);
    }

    public void updateNotRealized(User user, Service service) {
        notRealized++;

        Map<Service, RelationStats> serviceRelationStatsMap = relationStatsMap.get(user);
        if(serviceRelationStatsMap == null) {
            serviceRelationStatsMap = new HashMap<>();
            RelationStats relationStats = new RelationStats();
            relationStats.updateNotRealized();
            serviceRelationStatsMap.put(service, relationStats);
            relationStatsMap.put(user, serviceRelationStatsMap);
        } else {
            RelationStats relationStats = serviceRelationStatsMap.get(service);
            if(relationStats == null) {
                relationStats = new RelationStats();
                relationStats.updateNotRealized();
                serviceRelationStatsMap.put(service, relationStats);
            } else {
                relationStats.updateNotRealized();
            }
        }
    }

}
