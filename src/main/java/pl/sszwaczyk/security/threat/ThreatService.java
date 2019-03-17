package pl.sszwaczyk.security.threat;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.threat.generator.UniformThreatsGenerator;
import pl.sszwaczyk.security.threat.web.ThreatWebRoutable;

import java.time.LocalTime;
import java.util.*;

public class ThreatService implements IFloodlightModule, IThreatService {

    protected static final Logger log = LoggerFactory.getLogger(ThreatService.class);

    private IRestApiService restApiService;
    private IOFSwitchService switchService;
    private IRoutingService routingService;

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
        l.add(IOFSwitchService.class);
        l.add(IRoutingService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        routingService = context.getServiceImpl(IRoutingService.class);

        Map<String, String> configParameters = context.getConfigParams(this);
        boolean enableThreatsGenerator = Boolean.parseBoolean(configParameters.get("enable-threats-generator"));
        if(enableThreatsGenerator) {
            log.info("Threats generator enabled. Running threats generator...");
            UniformThreatsGenerator uniformThreatsGenerator = UniformThreatsGenerator.builder()
                    .threatService(this)
                    .routingService(routingService)
                    .switchService(switchService)
                    .minGap(Integer.parseInt(configParameters.get("min-gap")))
                    .maxGap(Integer.parseInt(configParameters.get("max-gap")))
                    .minDuration(Integer.parseInt(configParameters.get("min-duration")))
                    .maxDuration(Integer.parseInt(configParameters.get("max-duration")))
                    .build();

            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            uniformThreatsGenerator.start();
                        }
                    },
                    Long.parseLong(configParameters.get("threats-generator-start-time")) * 1000
            );
        }
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
        threat.setStartTime(LocalTime.now());
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
                new TimerTask() {
                    @Override
                    public void run() {
                        stopThreat(threat);
                    }
                },
                threat.getDuration() * 1000
        );
    }
}
