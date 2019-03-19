package pl.sszwaczyk.repository.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.repository.Flow;
import pl.sszwaczyk.repository.ISecureFlowsRepository;

import java.util.List;

public class PendingFlowsResource extends ServerResource {

    @Get("pending-flows")
    public List<Flow> getPendingFlows() {
        ISecureFlowsRepository secureFlowsRepository =
                (ISecureFlowsRepository) getContext().getAttributes().
                        get(ISecureFlowsRepository.class.getCanonicalName());

        return secureFlowsRepository.getPendingFlows();
    }

}
