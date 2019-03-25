package pl.sszwaczyk.repository.flow.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.repository.flow.Flow;
import pl.sszwaczyk.repository.flow.ISecureFlowsRepository;

import java.util.List;

public class FinishedFlowsResource extends ServerResource {

    @Get("finished-flows")
    public List<Flow> getFinishedFlows() {
        ISecureFlowsRepository secureFlowsRepository =
                (ISecureFlowsRepository) getContext().getAttributes().
                        get(ISecureFlowsRepository.class.getCanonicalName());

        return secureFlowsRepository.getFinishedFlows();
    }

}
