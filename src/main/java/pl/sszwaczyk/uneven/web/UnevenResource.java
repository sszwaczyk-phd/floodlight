package pl.sszwaczyk.uneven.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.uneven.IUnevenService;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.util.Map;

public class UnevenResource extends ServerResource {

    @Get("all")
    public Map<UnevenMetric, Double> calculateUneven() {
        IUnevenService unevenService =
                (IUnevenService) getContext().getAttributes().
                        get(IUnevenService.class.getCanonicalName());

        return unevenService.getUneven();
    }
}
