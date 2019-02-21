package pl.sszwaczyk.routing;

import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import pl.sszwaczyk.service.Service;

public interface ISecureRoutingService  extends IRoutingService {

    Path getSecurePath(Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);

}
