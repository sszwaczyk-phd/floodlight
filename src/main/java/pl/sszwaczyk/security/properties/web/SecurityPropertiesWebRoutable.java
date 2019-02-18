package pl.sszwaczyk.security.properties.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class SecurityPropertiesWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/switches", SecurityPropertiesResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/security/properties";
    }
}
