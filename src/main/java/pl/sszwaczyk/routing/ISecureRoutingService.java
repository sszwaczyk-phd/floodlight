package pl.sszwaczyk.routing;

import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;

public interface ISecureRoutingService  extends IRoutingService {

    Path getSecurePath(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);

}
