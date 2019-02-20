package pl.sszwaczyk.security.soc;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.SecurityDimension;
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
        update.setSrc(threat.getSrc());
        Map<SecurityDimension, Float> securityProperties = new HashMap<>();
        if(threat.getDst() == null) {
            update.setType(SOCUpdateType.THREAT_ACTIVATED_SWITCH);
            securityProperties.put(SecurityDimension.TRUST, 0.1f);
        } else {
            update.setSrcPort(threat.getSrcPort());
            update.setDst(threat.getDst());
            update.setDstPort(threat.getDstPort());
            update.setType(SOCUpdateType.THREAT_ACTIVATED_LINK);
            securityProperties.put(SecurityDimension.CONFIDENTIALITY, 0.1f);
            securityProperties.put(SecurityDimension.INTEGRITY, 0.1f);
            securityProperties.put(SecurityDimension.AVAILABILITY, 0.1f);
        }
        update.setSecurityPropertiesDifference(securityProperties);

        log.info("Sending SOCUpdate to listeners...");
        for(ISOCListener listener: listeners) {
            listener.socUpdate(update);
        }

        actualThreats.put(threat, securityProperties);
    }

    @Override
    public void threatEnded(Threat threat) {
        log.info("Got info about threat {} ended", threat);

        SOCUpdate update = new SOCUpdate();
        update.setSrc(threat.getSrc());
        Map<SecurityDimension, Float> securityProperties = new HashMap<>();
        if(threat.getDst() == null) {
            update.setType(SOCUpdateType.THREAT_ENDED_SWITCH);
            securityProperties.put(SecurityDimension.TRUST, 0.1f);
        } else {
            update.setSrcPort(threat.getSrcPort());
            update.setDst(threat.getDst());
            update.setDstPort(threat.getDstPort());
            update.setType(SOCUpdateType.THREAT_ENDED_LINK);
            securityProperties.put(SecurityDimension.CONFIDENTIALITY, 0.1f);
            securityProperties.put(SecurityDimension.INTEGRITY, 0.1f);
            securityProperties.put(SecurityDimension.AVAILABILITY, 0.1f);
        }
        update.setSecurityPropertiesDifference(securityProperties);

        log.info("Sending SOCUpdate to listeners...");
        for(ISOCListener listener: listeners) {
            listener.socUpdate(update);
        }

        actualThreats.remove(threat);
    }
}
