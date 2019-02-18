package pl.sszwaczyk.security.threat;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.soc.ISOCService;

import java.util.*;

public class ThreatService implements IFloodlightModule, IThreatService {

    protected static final Logger log = LoggerFactory.getLogger(ThreatService.class);

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
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {

                    try {
                        log.info("Sleeping 20s");
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Threat threat = Threat.builder()
                            .id(UUID.randomUUID().toString())
                            .src(DatapathId.of("00:00:00:00:00:00:00:01"))
                            .build();
                    log.info("Sending new threat started info");
                    for(IThreatListener listener: listeners) {
                        listener.threatStarted(threat);
                    }

                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    log.info("Sending threat ended info");
                                    for(IThreatListener listener: listeners) {
                                        listener.threatEnded(threat);
                                    }
                                }
                            },
                            10000
                    );
                }

            }
        }).start();
    }

    @Override
    public void addListener(IThreatListener listener) {
        listeners.add(listener);
    }
}
