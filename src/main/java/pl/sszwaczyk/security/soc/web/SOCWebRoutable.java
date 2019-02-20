package pl.sszwaczyk.security.soc.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class SOCWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/threats", SOCResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/security/soc";
    }
}
