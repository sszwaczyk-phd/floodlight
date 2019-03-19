package pl.sszwaczyk.repository;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Path;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.utils.AddressesAndPorts;

import java.util.List;
import java.util.Map;

public interface ISecureFlowsRepository extends IFloodlightService {

    void registerFlow(Flow flow);

    List<Flow> getFinishedFlows();

    Flow getPendingFlow(AddressesAndPorts addressesAndPorts);

    List<Flow> getPendingFlows();

    void addDecision(AddressesAndPorts ap, Decision decision);

    Map<AddressesAndPorts, Path> getActualPaths();

    void deleteActualPath(AddressesAndPorts ap);

}
