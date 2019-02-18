package pl.sszwaczyk.service;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SecurityPropertiesService implements IFloodlightModule, IOFSwitchListener, ILinkDiscoveryListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityPropertiesService.class);

    private IOFSwitchService switchService;
    private ILinkDiscoveryService linkService;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IOFSwitchService.class);
        l.add(ILinkDiscoveryService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        this.switchService = context.getServiceImpl(IOFSwitchService.class);
        this.linkService = context.getServiceImpl(ILinkDiscoveryService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        switchService.addOFSwitchListener(this);
        linkService.addListener(this);
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        log.debug("Switch added handling ({})", switchId);
        IOFSwitch newSwitch = switchService.getSwitch(switchId);
        newSwitch.getAttributes().put("TRUST", 0.99f);
        log.info("Trust for new switch {} set to 0.99", switchId);
    }

    @Override
    public void switchRemoved(DatapathId switchId) {
    }

    @Override
    public void switchActivated(DatapathId switchId) {
    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
    }

    @Override
    public void switchChanged(DatapathId switchId) {
    }

    @Override
    public void switchDeactivated(DatapathId switchId) {
    }

    @Override
    public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
        for(LDUpdate ldUpdate: updateList) {
            if(ldUpdate.getOperation().equals(UpdateOperation.LINK_UPDATED)) {
                for(Link link: linkService.getLinks().keySet()) {
                    if(link.getSrc().equals(ldUpdate.getSrc())
                        && link.getDst().equals(ldUpdate.getDst())
                        && link.getSrcPort().equals(ldUpdate.getSrcPort())
                        && link.getDstPort().equals(ldUpdate.getDstPort())) {
                        log.debug("Handling link update ({})", link);
                        link.setConfidentiality(0.99f);
                        link.setIntegrity(0.99f);
                        link.setAvailability(0.99f);
                        log.info("C, I, A for new link {} set to 0.99", link);
                    }
                }
            }
        }
    }
}
