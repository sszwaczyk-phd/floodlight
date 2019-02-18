package pl.sszwaczyk.security.threat.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class ThreatWebRoutable implements RestletRoutable {

    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/generate", GenerateThreatResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/security/threat";
    }
}
