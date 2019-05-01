package pl.sszwaczyk.security.soc;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.soc.calculator.RandomSameValueThreatInfluenceCalculator;
import pl.sszwaczyk.security.soc.calculator.RandomThreatInfluenceCalculator;
import pl.sszwaczyk.security.soc.calculator.ThreatInfluenceCalculator;
import pl.sszwaczyk.security.soc.web.SOCWebRoutable;
import pl.sszwaczyk.security.threat.IThreatListener;
import pl.sszwaczyk.security.threat.IThreatService;
import pl.sszwaczyk.security.threat.Threat;
import pl.sszwaczyk.statistics.ISecureRoutingStatisticsService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SOCService implements IFloodlightModule, ISOCService, IThreatListener {

    protected static final Logger log = LoggerFactory.getLogger(SOCService.class);

    private List<ISOCListener> listeners = new ArrayList<>();

    private IThreatService threatService;
    private IRestApiService restApiService;
    private ISecureRoutingStatisticsService secureRoutingStatisticsService;

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
        l.add(ISecureRoutingStatisticsService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        this.threatService = context.getServiceImpl(IThreatService.class);
        this.restApiService = context.getServiceImpl(IRestApiService.class);
        this.secureRoutingStatisticsService = context.getServiceImpl(ISecureRoutingStatisticsService.class);

        Map<String, String> configParameters = context.getConfigParams(this);
        String calculator = configParameters.get("threat-influence-calculator");
        if(calculator == null || calculator.isEmpty()) {
            throw new FloodlightModuleException("Threat influence calculator not specified");
        } else if(calculator.equals("random")) {
            String randomMinTString = configParameters.get("random-min-T");
            String randomMaxTString = configParameters.get("random-max-T");
            if(randomMinTString == null || randomMaxTString == null || randomMinTString.isEmpty() || randomMaxTString.isEmpty()) {
                throw new FloodlightModuleException("Random threat influcence calculator need random-min-T and random-max-T specified");
            }

            String randomMinCString = configParameters.get("random-min-C");
            String randomMaxCString = configParameters.get("random-max-C");
            if(randomMinCString == null || randomMaxCString == null || randomMinCString.isEmpty() || randomMaxCString.isEmpty()) {
                throw new FloodlightModuleException("Random threat influcence calculator need random-min-C and random-max-C specified");
            }

            String randomMinIString = configParameters.get("random-min-I");
            String randomMaxIString = configParameters.get("random-max-I");
            if(randomMinIString == null || randomMaxIString == null || randomMinIString.isEmpty() || randomMaxIString.isEmpty()) {
                throw new FloodlightModuleException("Random threat influcence calculator need random-min-I and random-max-I specified");
            }

            String randomMinAString = configParameters.get("random-min-A");
            String randomMaxAString = configParameters.get("random-max-A");
            if(randomMinAString == null || randomMaxAString == null || randomMinAString.isEmpty() || randomMaxAString.isEmpty()) {
                throw new FloodlightModuleException("Random threat influcence calculator need random-min-A and random-max-A specified");
            }

            Double minT = Double.valueOf(randomMinTString);
            Double maxT = Double.valueOf(randomMaxTString);

            Double minC = Double.valueOf(randomMinCString);
            Double maxC = Double.valueOf(randomMaxCString);

            Double minI = Double.valueOf(randomMinIString);
            Double maxI = Double.valueOf(randomMaxIString);

            Double minA = Double.valueOf(randomMinAString);
            Double maxA = Double.valueOf(randomMaxAString);

            threatInfluenceCalculator = RandomThreatInfluenceCalculator.builder()
                    .seed(Integer.parseInt(configParameters.get("random-seed")))
                    .minT(minT)
                    .maxT(maxT)
                    .minC(minC)
                    .maxC(maxC)
                    .minI(minI)
                    .maxI(maxI)
                    .minA(minA)
                    .maxA(maxA)
                    .build();

            log.info("RandomThreatInfluenceCalculator with minT = " + minT + "; maxT = " + maxT + "; minC = " + minC + "; maxC = " + maxC + "; minI = " + minI + "; maxI = " + maxI + "; minA = " + minA + "; maxA = " + maxA);
        } else if(calculator.equals("random-same")) {
            String randomMinString = configParameters.get("random-min");
            String randomMaxString = configParameters.get("random-max");
            if(randomMinString == null || randomMaxString == null || randomMinString.isEmpty() || randomMaxString.isEmpty()) {
                throw new FloodlightModuleException("Random threat influcence calculator need random-min and random-max specified");
            }
            double randomMin = Double.parseDouble(randomMinString);
            double randomMax = Double.parseDouble(randomMaxString);

            String influenceTString = configParameters.get("influence-t");
            boolean influenceT = false;
            if(influenceTString == null || influenceTString.isEmpty()) {
                log.info("Influence T not specified. Set default to " + influenceT);
            } else {
                influenceT = Boolean.parseBoolean(influenceTString);
                log.info("Influence T set to " + influenceT);
            }

            String influenceCString = configParameters.get("influence-c");
            boolean influenceC = false;
            if(influenceCString == null || influenceCString.isEmpty()) {
                log.info("Influence C not specified. Set default to " + influenceC);
            } else {
                influenceC = Boolean.parseBoolean(influenceCString);
                log.info("Influence C set to " + influenceC);
            }

            String influenceIString = configParameters.get("influence-i");
            boolean influenceI = false;
            if(influenceIString == null || influenceIString.isEmpty()) {
                log.info("Influence I not specified. Set default to " + influenceI);
            } else {
                influenceI = Boolean.parseBoolean(influenceIString);
                log.info("Influence I set to " + influenceI);
            }

            String influenceAString = configParameters.get("influence-a");
            boolean influenceA = false;
            if(influenceAString == null || influenceAString.isEmpty()) {
                log.info("Influence A not specified. Set default to " + influenceA);
            } else {
                influenceA = Boolean.parseBoolean(influenceAString);
                log.info("Influence A set to " + influenceA);
            }

            threatInfluenceCalculator = RandomSameValueThreatInfluenceCalculator.builder()
                    .seed(Integer.parseInt(configParameters.get("random-seed")))
                    .min(randomMin)
                    .max(randomMax)
                    .influenceT(influenceT)
                    .influenceC(influenceC)
                    .influenceI(influenceI)
                    .influenceA(influenceA)
                    .build();

            log.info("RandomSameValueThreatInfluenceCalculator with min = " + randomMin + ", max = " + randomMax + ", influence T = " + influenceT + ", influence C = " + influenceC + ", influence I = " + influenceI + ", influence A = " + influenceA );
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
        log.debug("Got info about threat {} started", threat);

        SOCUpdate update = new SOCUpdate();
        update.setType(SOCUpdateType.THREAT_ACTIVATED);
        update.setSwitches(threat.getSwitches());
        Map<SecurityDimension, Float> influence = threatInfluenceCalculator.calculateThreatInfluence(threat);
        update.setSecurityPropertiesDifference(influence);

        log.debug("Sending SOCUpdate to listeners...");
        for(ISOCListener listener: listeners) {
            listener.socUpdate(update);
        }

        actualThreats.put(threat, influence);
        secureRoutingStatisticsService.getSecureRoutingStatistics().addThreat(threat, influence);
    }

    @Override
    public void threatEnded(Threat threat) {
        log.debug("Got info about threat {} ended", threat);

        SOCUpdate update = new SOCUpdate();
        update.setType(SOCUpdateType.THREAT_ENDED);
        update.setSwitches(threat.getSwitches());
        update.setSecurityPropertiesDifference(actualThreats.get(threat));

        log.debug("Sending SOCUpdate to listeners...");
        for(ISOCListener listener: listeners) {
            listener.socUpdate(update);
        }

        actualThreats.remove(threat);
    }
}
