package pl.sszwaczyk.security.dtsp.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class DTSPWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/json", DTSPResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/security/dtsp";
    }
}
