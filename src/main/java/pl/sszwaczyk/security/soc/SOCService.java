package pl.sszwaczyk.security.soc;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.util.ParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.soc.calculator.RandomThreatInfluenceCalculator;
import pl.sszwaczyk.security.soc.calculator.ThreatInfluenceCalculator;
import pl.sszwaczyk.security.soc.web.SOCWebRoutable;
import pl.sszwaczyk.security.threat.IThreatListener;
import pl.sszwaczyk.security.threat.IThreatService;
import pl.sszwaczyk.security.threat.Threat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SOCService implements IFloodlightModule, ISOCService, IThreatListener {

    protected static final Logger log = LoggerFactory.getLogger(SOCService.class);

    private List<ISOCListener> listeners = new ArrayList<>();

    private IThreatService threatService;
    private IRestApiService restApiService;

    private Map<Threat, Map<SecurityDimension, Float>> actualThreats = new ConcurrentHashMap<>();

    private ThreatInfluenceCalculator threatInfluenceCalculator;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(ISOCService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(ISOCService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IThreatService.class);
        l.add(IRestApiService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        this.threatService = context.getServiceImpl(IThreatService.class);
        this.restApiService = context.getServiceImpl(IRestApiService.class);

        Map<String, String> configParameters = context.getConfigParams(this);
        String calculator = configParameters.get("threat-influence-calculator");
        if(calculator == null || calculator.isEmpty()) {
            throw new FloodlightModuleException("Threat influence calculator not specified");
        } else if(calculator.equals("random")) {
            String randomMinString = configParameters.get("random-min");
            String randomMaxString = configParameters.get("random-max");
            if(randomMinString == null || randomMaxString == null || randomMinString.isEmpty() || randomMaxString.isEmpty()) {
                throw new FloodlightModuleException("Random threat influcence calculator need random-min and random-max specified");
            }

            Double min = Double.valueOf(randomMinString);
            Double max = Double.valueOf(randomMaxString);
            threatInfluenceCalculator = RandomThreatInfluenceCalculator.builder()
                    .min(min)
                    .max(max)
                    .build();
            log.info("RandomThreatInfluenceCalculator with min {} and max {} initialized", min, max);
        } else {
            throw new FloodlightModuleException("Invalid threat influence calculator specified");
        }
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        threatService.addListener(this);
        restApiService.addRestletRoutable(new SOCWebRoutable());
    }

    @Override
    public void addListener(ISOCListener listener) {
        listeners.add(listener);
    }

    @Override
    public Map<Threat, Map<SecurityDimension, Float>> getActualThreats() {
        return actualThreats;
    }

    @Override
    public void threatStarted(Threat threat) {
        log.info("Got info about threat {} started", threat);

        SOCUpdate update = new SOCUpdate();
        update.setType(SOCUpdateType.THREAT_ACTIVATED);
        update.setSwitches(threat.getSwitches());
        Map<SecurityDimension, Float> influence = threatInfluenceCalculator.calculateThreatInfluence(threat);
        update.setSecurityPropertiesDifference(influence);

        log.info("Sending SOCUpdate to listeners...");
        for(ISOCListener listener: listeners) {
            listener.socUpdate(update);
        }

        actualThreats.put(threat, influence);
    }

    @Override
    public void threatEnded(Threat threat) {
        log.info("Got info about threat {} ended", threat);

        SOCUpdate update = new SOCUpdate();
        update.setType(SOCUpdateType.THREAT_ENDED);
        update.setSwitches(threat.getSwitches());
        update.setSecurityPropertiesDifference(actualThreats.get(threat));

        log.info("Sending SOCUpdate to listeners...");
        for(ISOCListener listener: listeners) {
            listener.socUpdate(update);
        }

        actualThreats.remove(threat);
    }
}
