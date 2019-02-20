package pl.sszwaczyk.security.soc.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.soc.ISOCService;
import pl.sszwaczyk.security.threat.Threat;

import java.util.Map;

public class SOCResource extends ServerResource {

    @Get("threats")
    public Map<Threat, Map<SecurityDimension, Float>> getActualThreats() {
        ISOCService socService =
                (ISOCService) getContext().getAttributes().
                        get(ISOCService.class.getCanonicalName());

        return socService.getActualThreats();
    }
}
