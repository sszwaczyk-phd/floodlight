package pl.sszwaczyk.security.threat;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingService;
import org.apache.commons.collections4.CollectionUtils;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.threat.generator.IThreatGenerator;
import pl.sszwaczyk.security.threat.generator.ITimeBetweenThreatsGenerator;
import pl.sszwaczyk.security.threat.generator.UniformThreatsGenerator;
import pl.sszwaczyk.security.threat.generator.UniformTimeBetweenThreatsGenerator;
import pl.sszwaczyk.security.threat.web.ThreatWebRoutable;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class ThreatService implements IFloodlightModule, IThreatService {

    protected static final Logger log = LoggerFactory.getLogger(ThreatService.class);

    private IRestApiService restApiService;
    private IOFSwitchService switchService;
    private IRoutingService routingService;

    private List<IThreatListener> listeners = new ArrayList<>();

    private List<Threat> actualThreats = new ArrayList<>();
    private List<DatapathId> actualAttackedSwitches = new ArrayList<>();

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
            String onlySwitchString = configParameters.get("only-switch");
            boolean onlySwitch;
            if(onlySwitchString != null && !onlySwitchString.isEmpty()) {
                onlySwitch = Boolean.parseBoolean(onlySwitchString);
                log.info("Only switch configured to " + onlySwitch);
            } else {
                onlySwitch = false;
                log.info("Only switch not configured. Set default to " + onlySwitch);
            }
            String onlyPathString = configParameters.get("only-path");
            boolean onlyPath;
            if(onlyPathString != null && !onlyPathString.isEmpty()) {
                onlyPath = Boolean.parseBoolean(onlyPathString);
            } else {
                onlyPath = false;
                log.info("Only path not configured. Set default to " + onlyPath);
            }
            int seed = Integer.parseInt(configParameters.get("random-seed"));
            int minDuration = Integer.parseInt(configParameters.get("min-duration"));
            int maxDuration = Integer.parseInt(configParameters.get("max-duration"));
            UniformThreatsGenerator uniformThreatsGenerator = new UniformThreatsGenerator(switchService, routingService,
                    seed, minDuration, maxDuration, onlySwitch, onlyPath);

            int minGap = Integer.parseInt(configParameters.get("min-gap"));
            int maxGap = Integer.parseInt(configParameters.get("max-gap"));
            ITimeBetweenThreatsGenerator timeBetweenThreatsGenerator = new UniformTimeBetweenThreatsGenerator(seed, minGap, maxGap);

            long startTime = Long.parseLong(configParameters.get("threats-generator-start-time"));
            log.info("Threats generator start time set to " + startTime + " seconds");
            boolean canAttackSameSwitch = Boolean.parseBoolean(configParameters.get("can-attack-same-switch"));

            scheduleThreatsGeneration(startTime, uniformThreatsGenerator, timeBetweenThreatsGenerator, canAttackSameSwitch);
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

        actualThreats.add(threat);
        log.info("Threat {} added to actual threats.", threat);

        actualAttackedSwitches.addAll(threat.getSwitches());
        log.info("Attacked switches: {} added to actual attacked switches.",
                threat.getSwitches().stream().map(DatapathId::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public void stopThreat(Threat threat) {
        log.info("Stopping threat {}", threat);
        for(IThreatListener listener: listeners) {
            listener.threatEnded(threat);
        }

        actualThreats.remove(threat);
        log.info("Threat {} removed from actual threats.", threat);

        actualAttackedSwitches.removeAll(threat.getSwitches());
        log.info("Switches {} removed from attacked switches.",
                threat.getSwitches().stream().map(DatapathId::toString).collect(Collectors.joining(", ")));

    }

    public void threatGenerationTask(IThreatGenerator threatGenerator, ITimeBetweenThreatsGenerator timeBetweenThreatsGenerator, boolean canAttackSameSwitch) {
        while (true) {
            if (!canAttackSameSwitch && actualAttackedSwitches.containsAll(switchService.getAllSwitchDpids())) {
                log.info("All switches attacked and cannot attack same switch twice.");
                sleepToNextThreat(timeBetweenThreatsGenerator.generateTimeBetweenThreats());
                continue;
            }

            Threat threat = threatGenerator.generateThreat();
            boolean isAnySwitchAlreadyAttacked = CollectionUtils.containsAny(actualAttackedSwitches, threat.getSwitches());
            if (!canAttackSameSwitch && isAnySwitchAlreadyAttacked) {
                log.info("Threat contains already attacked switch and cannot attack same switch twice.");
                continue;
            }

            startThreat(threat);

            sleepToNextThreat(timeBetweenThreatsGenerator.generateTimeBetweenThreats());
        }
    }

    private void sleepToNextThreat(long time) {
        try {
            log.info("Sleeping between next threat generation for {} seconds", time / 1000);
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void scheduleThreatsGeneration(long delay, IThreatGenerator uniformThreatsGenerator,
                                           ITimeBetweenThreatsGenerator timeBetweenThreatsGenerator,
                                           boolean canAttackSameSwitch) {
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        threatGenerationTask(uniformThreatsGenerator, timeBetweenThreatsGenerator, canAttackSameSwitch);
                    }
                },
                delay * 1000
        );
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
