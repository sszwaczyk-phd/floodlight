package pl.sszwaczyk.routing;

import net.floodlightcontroller.routing.IRoutingService;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;

public interface ISecureRoutingService  extends IRoutingService {

    Decision getSecureDecision(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);

    Decision getSecureShortestDecision(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);

}
