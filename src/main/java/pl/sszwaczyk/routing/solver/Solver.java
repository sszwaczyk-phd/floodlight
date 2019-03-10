package pl.sszwaczyk.routing.solver;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;

public interface Solver {

    Decision solve(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);
}
