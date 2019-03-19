package pl.sszwaczyk.repository.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class SecureFlowsRepositoryRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/actual-paths", ActualPathsResource.class);
        router.attach("/pending-flows", PendingFlowsResource.class);
        router.attach("/finished-flows", FinishedFlowsResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/security/flows";
    }
}
