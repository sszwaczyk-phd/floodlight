package pl.sszwaczyk.repository.flow.web;

import net.floodlightcontroller.routing.Path;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.repository.flow.ISecureFlowsRepository;
import pl.sszwaczyk.utils.AddressesAndPorts;

import java.util.Map;

public class ActualPathsResource extends ServerResource {

    @Get("actual-paths")
    public Map<AddressesAndPorts, Path> getActualSecurityFlows() {
        ISecureFlowsRepository secureFlowsRepository =
                (ISecureFlowsRepository) getContext().getAttributes().
                        get(ISecureFlowsRepository.class.getCanonicalName());

        return secureFlowsRepository.getActualPaths();
    }

}
