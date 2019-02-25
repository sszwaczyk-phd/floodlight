package pl.sszwaczyk.statistics.web;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.statistics.ISecureRoutingStatisticsService;

import java.util.UUID;

public class SnapshotSecureRoutingStatisticsResource extends ServerResource {

    @Post("snapshot")
    public String snapshotSecureRoutingStatistics() {
        ISecureRoutingStatisticsService statisticsService =
                (ISecureRoutingStatisticsService) getContext().getAttributes().
                        get(ISecureRoutingStatisticsService.class.getCanonicalName());

        return statisticsService.snapshotStatisticsToFile("/tmp/" + UUID.randomUUID().toString() + ".xlsx");
    }

}
