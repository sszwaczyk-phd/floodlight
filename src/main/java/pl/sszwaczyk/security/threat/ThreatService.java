package pl.sszwaczyk.security.threat;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.soc.ISOCService;
import pl.sszwaczyk.security.threat.web.ThreatWebRoutable;

import java.util.*;

public class ThreatService implements IFloodlightModule, IThreatService {

    protected static final Logger log = LoggerFactory.getLogger(ThreatService.class);

    private IRestApiService restApiService;

    private List<IThreatListener> listeners = new ArrayList<>();

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(IThreatService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IThreatService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IRestApiService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new ThreatWebRoutable());
    }

    @Override
    public void addListener(IThreatListener listener) {
        listeners.add(listener);
    }

    @Override
    public void startThreat(Threat threat) {
        log.info("Starting threat {}", threat);
        for(IThreatListener listener: listeners) {
            listener.threatStarted(threat);
            scheduleThreatEnd(threat);
        }
    }

    @Override
    public void stopThreat(Threat threat) {
        log.info("Stopping threat {}", threat);
        for(IThreatListener listener: listeners) {
            listener.threatEnded(threat);
        }
    }

    private void scheduleThreatEnd(Threat threat) {
        new Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        stopThreat(threat);
                    }
                },
                threat.getDuration() * 1000
        );
    }
}
