package pl.sszwaczyk.routing.solver;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import pl.sszwaczyk.service.Service;

public interface Solver {

    SolveResult solve(Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort);
}
