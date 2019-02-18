package pl.sszwaczyk.security.dtsp.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.security.dtsp.DTSP;
import pl.sszwaczyk.security.dtsp.IDTSPService;

import java.util.List;

public class DTSPResource extends ServerResource {

    @Get("/json")
    public List<DTSP> getAllDTSPs() {
        IDTSPService dtspService =
                (IDTSPService) getContext().getAttributes().
                        get(IDTSPService.class.getCanonicalName());

        return dtspService.getAllDTSPs();
    }
}
