package pl.sszwaczyk.repository.link.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class LinkStatisticsRepositoryRoutable implements RestletRoutable {


    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/max-bandwidth", MaxBandwidthResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/security/links/stats";
    }
}
