package pl.sszwaczyk.statistics.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class SecureRoutingStatisticsRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/json", SecureRoutingStatisticsResource.class);
        router.attach("/snapshot", SnapshotSecureRoutingStatisticsResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/security/stats";
    }
}
