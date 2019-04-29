package pl.sszwaczyk.path.calculator;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.routing.Path;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PathPropertiesCalculator {

    protected IOFSwitchService switchService;
    protected ILinkDiscoveryService linkService;

    public PathPropertiesCalculator(IOFSwitchService switchService,
                                    ILinkDiscoveryService linkService) {
        this.switchService = switchService;
        this.linkService = linkService;
    }

    public Map<SecurityDimension, Float> calculatePathProperties(Path path) {
        List<NodePortTuple> npts = path.getPath();
        List<IOFSwitch> switches = new ArrayList<>();
        List<Link> links = new ArrayList<>();
        if(npts.get(0).getNodeId().equals(npts.get(1).getNodeId())) {
            for(int i = 0; i < npts.size() - 2; i = i + 2) {
                IOFSwitch s1 = switchService.getSwitch(npts.get(i + 1).getNodeId());
                IOFSwitch s2 = switchService.getSwitch(npts.get(i + 2).getNodeId());
                Link l = linkService.getLink(s1.getId(), npts.get(i + 1).getPortId(), s2.getId(), npts.get(i + 2).getPortId());
                if(i == 0) {
                    switches.add(s1);
                }
                switches.add(s2);
                if(l != null) {
                    links.add(l);
                }
            }
        } else {
            for(int i = 0; i < npts.size() - 1; i = i + 2) {
                IOFSwitch s1 = switchService.getSwitch(npts.get(i).getNodeId());
                IOFSwitch s2 = switchService.getSwitch(npts.get(i + 1).getNodeId());
                Link l = linkService.getLink(s1.getId(), npts.get(i).getPortId(), s2.getId(), npts.get(i + 1).getPortId());
                if(i == 0) {
                    switches.add(s1);
                }
                switches.add(s2);
                if(l != null) {
                    links.add(l);
                }
            }
        }


        return calculatePathProperties(switches, links);
    }

    public abstract Map<SecurityDimension, Float> calculatePathProperties(List<IOFSwitch> switches, List<Link> links);
}
