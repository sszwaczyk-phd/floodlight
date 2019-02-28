package pl.sszwaczyk.repository;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Path;
import pl.sszwaczyk.utils.AddressesAndPorts;

import java.util.Map;

public interface ISecureFlowsRepository extends IFloodlightService {

    void registerFlow(AddressesAndPorts ap, Path path);

    Map<AddressesAndPorts, Path> getFlows();

    void deleteFlow(AddressesAndPorts ap);

}
