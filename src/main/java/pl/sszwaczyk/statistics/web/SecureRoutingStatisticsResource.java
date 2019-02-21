package pl.sszwaczyk.statistics.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.statistics.ISecureRoutingStatisticsService;
import pl.sszwaczyk.statistics.SecureRoutingStatistics;

public class SecureRoutingStatisticsResource extends ServerResource {

    @Get("json")
    public SecureRoutingStatistics getSecureRoutingStatistics() {
        ISecureRoutingStatisticsService statisticsService =
                (ISecureRoutingStatisticsService) getContext().getAttributes().
                        get(ISecureRoutingStatisticsService.class.getCanonicalName());

        return statisticsService.getSecureRoutingStatistics();
    }
}
