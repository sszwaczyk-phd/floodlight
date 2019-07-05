package pl.sszwaczyk.security.properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.properties.web.LinkSecurityProperties;
import pl.sszwaczyk.security.properties.web.SecurityPropertiesWebRoutable;
import pl.sszwaczyk.security.properties.web.SwitchSecurityProperties;
import pl.sszwaczyk.security.soc.ISOCListener;
import pl.sszwaczyk.security.soc.ISOCService;
import pl.sszwaczyk.security.soc.SOCUpdate;
import pl.sszwaczyk.security.soc.SOCUpdateType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class SecurityPropertiesService implements IFloodlightModule, IOFSwitchListener, ILinkDiscoveryListener,
        ISOCListener, ISecurityPropertiesService {

    private static final Logger log = LoggerFactory.getLogger(SecurityPropertiesService.class);

    private IRestApiService restApiService;
    private IOFSwitchService switchService;
    private ILinkDiscoveryService linkService;
    private ISOCService socService;
    private IStatisticsService statisticsService;
    private IThreadPoolService threadPoolService;

    private List<ISecurityPropertiesChangedListener> listeners = new ArrayList<>();

    private boolean enableLinkAvaialabilityUtilizationActualization = false;
    private boolean readFromFile = false;

    private Map<String, SwitchSecurityProperties> initSwitchesSecurityProperites = new HashMap<>();
    private Map<Link, LinkSecurityProperties> initLinkSecurityProperties = new HashMap<>();

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(ISecurityPropertiesService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(ISecurityPropertiesService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IRestApiService.class);
        l.add(IOFSwitchService.class);
        l.add(ILinkDiscoveryService.class);
        l.add(ISOCService.class);
        l.add(IStatisticsService.class);
        l.add(IThreadPoolService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        this.restApiService = context.getServiceImpl(IRestApiService.class);
        this.switchService = context.getServiceImpl(IOFSwitchService.class);
        this.linkService = context.getServiceImpl(ILinkDiscoveryService.class);
        this.socService = context.getServiceImpl(ISOCService.class);
        this.statisticsService = context.getServiceImpl(IStatisticsService.class);
        this.threadPoolService = context.getServiceImpl(IThreadPoolService.class);

        Map<String, String> configParameters = context.getConfigParams(this);
        String tmp = configParameters.get("enable-utilization-availability-actualization");
        if(tmp != null && !tmp.isEmpty()) {
            enableLinkAvaialabilityUtilizationActualization = Boolean.parseBoolean(tmp);
            log.info("Link availability actualization based on utliization set to " + enableLinkAvaialabilityUtilizationActualization);
        } else {
            enableLinkAvaialabilityUtilizationActualization = false;
            log.info("Link availability actualization based on utilization not set. Set default to " + enableLinkAvaialabilityUtilizationActualization);
        }

        tmp = configParameters.get("read-from-file");
        if(tmp != null && !tmp.isEmpty()) {
            readFromFile = Boolean.parseBoolean(tmp);
            log.info("Read Security Properties set to " + readFromFile);
            if(readFromFile) {
                String repositoryFile = configParameters.get("repository-file");
                if(repositoryFile == null || repositoryFile.isEmpty()) {
                    throw new FloodlightModuleException("Read Security Properties from file enabled but repository file not set!");
                }
                loadInitSecurityProperties(repositoryFile);
            }
        } else {
            readFromFile = false;
            log.info("Read Security Properties not set. Set default to " + readFromFile);
        }
    }

    private void loadInitSecurityProperties(String repositoryFile) throws FloodlightModuleException {
        initSwitchesSecurityProperites = new HashMap<>();
        initLinkSecurityProperties = new HashMap<>();

        File file = new File(repositoryFile);
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(file);
            JsonNode switchesNode = root.path("switches");
            List<SwitchSecurityProperties> switchesSecurityProps = mapper.readValue(mapper.treeAsTokens(switchesNode), new TypeReference<List<SwitchSecurityProperties>>() {});
            for(SwitchSecurityProperties p: switchesSecurityProps) {
                initSwitchesSecurityProperites.put(p.getSwitchDpid(), p);
            }

            JsonNode linksNode = root.path("links");
            List<LinkSecurityProperties> linkSecurityProps = mapper.readValue(mapper.treeAsTokens(linksNode), new TypeReference<List<LinkSecurityProperties>>() {});
            for(LinkSecurityProperties p: linkSecurityProps) {
                Link link = new Link(DatapathId.of(p.getSrc()), OFPort.of(p.getSrcPort()), DatapathId.of(p.getDst()), OFPort.of(p.getDstPort()), U64.ZERO);
                initLinkSecurityProperties.put(link, p);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new FloodlightModuleException("Cannot parse Security Properties from file");
        }


    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new SecurityPropertiesWebRoutable());
        switchService.addOFSwitchListener(this);
        linkService.addListener(this);
        socService.addListener(this);
        if(enableLinkAvaialabilityUtilizationActualization) {
            threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new LinkAvailabilityUtilizationActualizator(), 10, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        log.debug("Switch added handling ({})", switchId);
        IOFSwitch newSwitch = switchService.getSwitch(switchId);
        if(readFromFile) {
            SwitchSecurityProperties switchSecurityProperties = initSwitchesSecurityProperites.get(switchId.toString());
            if(switchSecurityProperties != null) {
                newSwitch.getAttributes().put(SecurityDimension.TRUST, switchSecurityProperties.getTrust());
                log.info("Trust for new switch {} set to {}", switchId, switchSecurityProperties.getTrust());
            } else {
                log.warn("Init Security Properties for switch " + switchId + " not found! Setting to 0.99.");
                newSwitch.getAttributes().put(SecurityDimension.TRUST, 0.99f);
            }
        } else {
            newSwitch.getAttributes().put(SecurityDimension.TRUST, 0.99f);
            log.info("Trust for new switch {} set to 0.99", switchId);
        }

//        SwitchSecurityProperties props = new SwitchSecurityProperties();
//        props.setSwitchDpid(switchId.toString());
//        props.setTrust((float) ThreadLocalRandom.current().nextDouble(0.85, 0.95));
//        initSwitchesSecurityProperites.put(switchId.toString(), props);
//
//        Runnable r = () -> {
//            String switches = null;
//            String links = null;
//            Collection<SwitchSecurityProperties> values = initSwitchesSecurityProperites.values();
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                switches = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(values);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//            Collection<LinkSecurityProperties> values1 = initLinkSecurityProperties.values();
//            try {
//                links = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(values1);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//
//            System.out.println(switches);
//            System.out.println(links);
//        };
//
//        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
//        scheduledExecutorService.scheduleWithFixedDelay(r, 100, 600, TimeUnit.SECONDS);
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
                        && link.getDstPort().equals(ldUpdate.getDstPort())
                        && link.getSecurityProperties() == null) {
                            log.debug("Handling link update ({})", link);
                            if(readFromFile) {
                                LinkSecurityProperties linkSecurityProperties = initLinkSecurityProperties.get(link);
                                if(linkSecurityProperties != null) {
                                    Map<SecurityDimension, Float> securityProperties = new HashMap<>();
                                    securityProperties.put(SecurityDimension.CONFIDENTIALITY, linkSecurityProperties.getConfidentiality());
                                    securityProperties.put(SecurityDimension.INTEGRITY, linkSecurityProperties.getIntegrity());
                                    securityProperties.put(SecurityDimension.AVAILABILITY, linkSecurityProperties.getAvailability());
                                    link.setSecurityProperties(securityProperties);
                                    log.info("C, I, A for new link " + link + " set to " + linkSecurityProperties.getConfidentiality() + ", " + linkSecurityProperties.getIntegrity() + ", " + linkSecurityProperties.getAvailability());
                                } else {
                                    log.warn("Init Security Properties for link " + link + " not found! Setting to 0.99.");
                                    Map<SecurityDimension, Float> securityProperties = new HashMap<>();
                                    securityProperties.put(SecurityDimension.CONFIDENTIALITY, 0.99f);
                                    securityProperties.put(SecurityDimension.INTEGRITY, 0.99f);
                                    securityProperties.put(SecurityDimension.AVAILABILITY, 0.99f);
                                    link.setSecurityProperties(securityProperties);
                                }
                            } else {
                                Map<SecurityDimension, Float> securityProperties = new HashMap<>();
                                securityProperties.put(SecurityDimension.CONFIDENTIALITY, 0.99f);
                                securityProperties.put(SecurityDimension.INTEGRITY, 0.99f);
                                securityProperties.put(SecurityDimension.AVAILABILITY, 0.99f);
                                link.setSecurityProperties(securityProperties);
                                log.info("C, I, A for new link {} set to 0.99", link);
                            }

                            LinkSecurityProperties p = new LinkSecurityProperties();
                            p.setSrc(link.getSrc().toString());
                            p.setSrcPort(link.getSrcPort().getPortNumber());
                            p.setDst(link.getDst().toString());
                            p.setDstPort(link.getDstPort().getPortNumber());
                            p.setAvailability(0.99f);
                            p.setIntegrity((float) ThreadLocalRandom.current().nextDouble(0.85, 0.95));
                            p.setConfidentiality((float) ThreadLocalRandom.current().nextDouble(0.85, 0.95));
                            initLinkSecurityProperties.put(link, p);
                    }
                }
            }
        }

    }

    @Override
    public void socUpdate(SOCUpdate socUpdate) {
        log.debug("SOC update {} received", socUpdate);

        SOCUpdateType type = socUpdate.getType();
        List<DatapathId> dpids = socUpdate.getSwitches();
        Map<SecurityDimension, Float> securityPropertiesDifference = socUpdate.getSecurityPropertiesDifference();

        List<IOFSwitch> switches = new ArrayList<>();
        if(type.equals(SOCUpdateType.THREAT_ACTIVATED)) {

            for(DatapathId dpid: dpids) {

                IOFSwitch s = switchService.getSwitch(dpid);
                switches.add(s);
                Float actualTrust = (Float) s.getAttributes().get(SecurityDimension.TRUST);
                Float trustDifference = securityPropertiesDifference.get(SecurityDimension.TRUST);
                if(trustDifference > actualTrust) {
                    log.warn("New threat TRUST difference more than actual TRUST for switch {}. Setting TRUST to 0.", dpid);
                    s.getAttributes().put(SecurityDimension.TRUST, 0.0f);
                } else {
                    s.getAttributes().put(SecurityDimension.TRUST, actualTrust - trustDifference);
                    log.debug("Set TRUST for switch {} to {}", dpid, s.getAttributes().get(SecurityDimension.TRUST));
                }

                Set<Link> links = linkService.getSwitchLinks().get(dpid);
                for(Link link: links) {
                    activateThreatOnLink(securityPropertiesDifference, link);
                }


            }

            sendUpdates(SecurityPropertiesUpdateType.PROPERTIES_DOWN, switches);

        } else if(type.equals(SOCUpdateType.THREAT_ENDED)) {

            for(DatapathId dpid: dpids) {

                IOFSwitch s = switchService.getSwitch(dpid);
                switches.add(s);
                Float actualTrust = (Float) s.getAttributes().get(SecurityDimension.TRUST);
                Float trustDifference = securityPropertiesDifference.get(SecurityDimension.TRUST);
                if(actualTrust + trustDifference > 0.99) {
                    log.warn("Threat ended TRUST plus actual TRUST is more than 0.99 for switch {}. Setting TRUST to 0.99", dpid);
                    s.getAttributes().put(SecurityDimension.TRUST, 0.99f);
                } else {
                    s.getAttributes().put(SecurityDimension.TRUST, actualTrust + trustDifference);
                    log.debug("Set TRUST for switch {} to {}", dpid, s.getAttributes().get(SecurityDimension.TRUST));
                }

                Set<Link> links = linkService.getSwitchLinks().get(dpid);
                for(Link link: links) {
                    deactivateThreatOnLink(securityPropertiesDifference, link);
                }

            }

            sendUpdates(SecurityPropertiesUpdateType.PROPERTIES_UP, switches);

        }

    }

    @Override
    public List<SwitchSecurityProperties> getSwitchesSecurityProperties() {
        List<SwitchSecurityProperties> properties = new ArrayList<>();

        Map<DatapathId, IOFSwitch> allSwitches = switchService.getAllSwitchMap();
        allSwitches.values().forEach(s -> {
            SwitchSecurityProperties props = new SwitchSecurityProperties();
            props.setSwitchDpid(s.getId().toString());
            props.setTrust((Float) s.getAttributes().get(SecurityDimension.TRUST));
            properties.add(props);
        });

        return properties;
    }

    @Override
    public List<LinkSecurityProperties> getLinksSecurityProperties() {
        List<LinkSecurityProperties> properties = new ArrayList<>();

        Map<Link, LinkInfo> allLinks = linkService.getLinks();
        allLinks.keySet().forEach(l -> {
            LinkSecurityProperties props = new LinkSecurityProperties();
            props.setSrc(l.getSrc().toString());
            props.setSrcPort(l.getSrcPort().getPortNumber());
            props.setDst(l.getDst().toString());
            props.setDstPort(l.getDstPort().getPortNumber());
            props.setConfidentiality(l.getConfidentiality());
            props.setIntegrity(l.getIntegrity());
            props.setAvailability(l.getAvailability());
            properties.add(props);
        });

        return properties;
    }

    @Override
    public void setSwitchSecurityProperties(SwitchSecurityProperties properties) {
        IOFSwitch s = switchService.getSwitch(DatapathId.of(properties.getSwitchDpid()));
        Float oldTrust = (Float) s.getAttributes().get(SecurityDimension.TRUST);
        Float newTrust = properties.getTrust();
        s.getAttributes().put(SecurityDimension.TRUST, newTrust);
        log.debug("Set TRUST for switch {} to {}",properties.getSwitchDpid(), newTrust);
        sendUpdates(oldTrust > newTrust ? SecurityPropertiesUpdateType.PROPERTIES_DOWN : SecurityPropertiesUpdateType.PROPERTIES_UP, Lists.newArrayList(s));
    }

    @Override
    public void setLinkSecurityProperties(LinkSecurityProperties properties) {
        Link link = linkService.getLink(DatapathId.of(properties.getSrc()),
                OFPort.of(properties.getSrcPort()),
                DatapathId.of(properties.getDst()),
                OFPort.of(properties.getDstPort()));
        if(link != null) {
            link.setConfidentiality(properties.getConfidentiality());
            link.setIntegrity(properties.getIntegrity());
            link.setAvailability(properties.getAvailability());
            log.debug("Set new C, I, A for link {}", link);
            log.debug("Confidentiality = {}", link.getConfidentiality());
            log.debug("Integrity = {}", link.getIntegrity());
            log.debug("Availability = {}", link.getAvailability());
        }
    }

    @Override
    public void addListener(ISecurityPropertiesChangedListener listener) {
        listeners.add(listener);
    }

    private void activateThreatOnLink(Map<SecurityDimension, Float> securityPropertiesDifference, Link link) {
        Float actualConfidentiality = link.getConfidentiality();
        Float confidentialityDifference = securityPropertiesDifference.get(SecurityDimension.CONFIDENTIALITY);
        if(confidentialityDifference > actualConfidentiality) {
            log.warn("New threat CONFIDENTIALITY difference more than actual CONFIDENTIALITY for link {}. Setting CONFIDENTIALITY to 0.", link);
            link.setConfidentiality(0.0f);
        } else {
            link.setConfidentiality(actualConfidentiality - confidentialityDifference);
        }

        Float actualIntegrity = link.getIntegrity();
        Float integrityDifference = securityPropertiesDifference.get(SecurityDimension.INTEGRITY);
        if(integrityDifference > actualIntegrity) {
            log.warn("New threat INTEGRITY difference more than actual INTEGRITY for link {}. Setting INTEGRITY to 0.", link);
            link.setIntegrity(0.0f);
        } else {
            link.setIntegrity(actualIntegrity - integrityDifference);
        }

        Float actualAvailability = link.getAvailability();
        Float availabilityDifference = securityPropertiesDifference.get(SecurityDimension.AVAILABILITY);
        if(availabilityDifference > actualAvailability) {
            log.warn("New threat AVAILABILITY difference more than actual AVAILABILITY for link {}. Setting AVAILABILITY to 0.", link);
            link.setAvailability(0.0f);
        } else {
            link.setAvailability(actualAvailability - availabilityDifference);
        }

        log.debug("Set new C, I, A for link {}", link);
        log.debug("Confidentiality = {}", link.getConfidentiality());
        log.debug("Integrity = {}", link.getIntegrity());
        log.debug("Availability = {}", link.getAvailability());
    }

    private void deactivateThreatOnLink(Map<SecurityDimension, Float> securityPropertiesDifference, Link link) {
        Float actualConfidentiality = link.getConfidentiality();
        Float confidentialityDifference = securityPropertiesDifference.get(SecurityDimension.CONFIDENTIALITY);
        if(actualConfidentiality + confidentialityDifference > 0.99) {
            log.warn("Threat ended CONFIDENTIALITY plus actual CONFIDENTIALITY for link {} is more than 0.99. Setting CONFIDENTIALITY to 0.99.", link);
            link.setConfidentiality(0.99f);
        } else {
            link.setConfidentiality(actualConfidentiality + confidentialityDifference);
        }

        Float actualIntegrity = link.getIntegrity();
        Float integrityDifference = securityPropertiesDifference.get(SecurityDimension.INTEGRITY);
        if(actualIntegrity + integrityDifference > 0.99) {
            log.warn("Threat ended INTEGRITY plus actual INTEGRITY for link {} is more than 0.99. Setting INTEGRITY to 0.99.", link);
            link.setIntegrity(0.99f);
        } else {
            link.setIntegrity(actualIntegrity + integrityDifference);
        }

        Float actualAvailability = link.getAvailability();
        Float availabilityDifference = securityPropertiesDifference.get(SecurityDimension.AVAILABILITY);
        if(actualAvailability + availabilityDifference > 0.99) {
            log.warn("Threat ended AVAILABILITY plus actual AVAILABILITY for link {} is more than 0.99. Setting AVAILABILITY to 0.99.", link);
            link.setAvailability(0.99f);
        } else {
            link.setAvailability(actualAvailability + availabilityDifference);
        }

        log.debug("Set new C, I, A for link {}", link);
        log.debug("Confidentiality = {}", link.getConfidentiality());
        log.debug("Integrity = {}", link.getIntegrity());
        log.debug("Availability = {}", link.getAvailability());
    }

    private void sendUpdates(SecurityPropertiesUpdateType type, List<IOFSwitch> switches) {
        SecurityPropertiesUpdate update = SecurityPropertiesUpdate.builder()
                .type(type)
                .switches(switches)
                .build();
        log.debug("Sending updates about security properties changed...");
        for(ISecurityPropertiesChangedListener l: listeners) {
            l.securityPropertiesChanged(update);
        }
    }

    class LinkAvailabilityUtilizationActualizator implements Runnable {

        @Override
        public void run() {
            log.debug("Updating link availability based on utilization...");
            Map<NodePortTuple, SwitchPortBandwidth> bandwidthConsumption = statisticsService.getBandwidthConsumption();
            for(Map.Entry<NodePortTuple, SwitchPortBandwidth> entry: bandwidthConsumption.entrySet()) {
                NodePortTuple npt = entry.getKey();
                log.debug("Updating npt " + npt);
                Link link = linkService.getLink(npt.getNodeId(), npt.getPortId());
                if(link == null) {
                    log.debug("Link to not found");
                    continue;
                }
                log.debug("Updating link " + link);
                SwitchPortBandwidth switchPortBandwidth = bandwidthConsumption.get(npt);
                double txUtilization = switchPortBandwidth.getTxUtilization();
                float linkAvailability = 0.99f - (float) txUtilization;
                link.setAvailability(linkAvailability);
                log.debug("Set link availability to " + linkAvailability + " due to utilization change");
            }

        }

    }
}
