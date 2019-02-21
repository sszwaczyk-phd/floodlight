package net.floodlightcontroller.routing;

import java.util.*;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.devicemanager.SwitchPort;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.topology.ITopologyManagerBackend;
import net.floodlightcontroller.topology.ITopologyService;
import pl.sszwaczyk.path.IPathPropertiesService;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.dtsp.DTSP;
import pl.sszwaczyk.security.dtsp.IDTSPService;
import pl.sszwaczyk.security.risk.IRiskCalculationService;
import pl.sszwaczyk.security.risk.Risks;
import pl.sszwaczyk.service.IServiceService;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.statistics.ISecureRoutingStatisticsService;

/**
 * Separate path-finding and routing functionality from the 
 * topology package. It makes sense to keep much of the core
 * code in the TopologyInstance, but the TopologyManger is
 * too confusing implementing so many interfaces and doing
 * so many tasks. This is a cleaner approach IMHO.
 * 
 * All routing and path-finding functionality is visible to
 * the rest of the controller via the IRoutingService implemented
 * by the RoutingManger (this). The RoutingManger performs
 * tasks it can perform locally, such as the handling of
 * IRoutingDecisionChangedListeners, while it defers to the
 * current TopologyInstance (exposed via the ITopologyManagerBackend
 * interface) for tasks best performed by the topology
 * package, such as path-finding.
 * 
 * @author rizard
 */
public class RoutingManager implements IFloodlightModule, IRoutingService {
    private Logger log = LoggerFactory.getLogger(RoutingManager.class);
    
    private static ITopologyManagerBackend tm;
    
    private List<IRoutingDecisionChangedListener> decisionChangedListeners;

    private static volatile boolean enableL3RoutingService = false;

    //Secure routing
    private IServiceService serviceService;
    private IRiskCalculationService riskService;
    private IDTSPService dtspService;
    private IPathPropertiesService pathPropertiesService;
    private ISecureRoutingStatisticsService secureRoutingStatisticsService;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return ImmutableSet.of(IRoutingService.class);
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return ImmutableMap.of(IRoutingService.class, this);
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(ITopologyService.class);

        //Secure routing
        l.add(IServiceService.class);
        l.add(IDTSPService.class);
        l.add(IRiskCalculationService.class);
        l.add(IPathPropertiesService.class);
        l.add(ISecureRoutingStatisticsService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        log.debug("RoutingManager starting up");
        tm = (ITopologyManagerBackend) context.getServiceImpl(ITopologyService.class);
        decisionChangedListeners = new ArrayList<IRoutingDecisionChangedListener>();

        //Secure routing
        this.dtspService = context.getServiceImpl(IDTSPService.class);
        this.riskService = context.getServiceImpl(IRiskCalculationService.class);
        this.pathPropertiesService = context.getServiceImpl(IPathPropertiesService.class);
        this.secureRoutingStatisticsService = context.getServiceImpl(ISecureRoutingStatisticsService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException { }

    @Override
    public void setPathMetric(PATH_METRIC metric) {
        tm.setPathMetric(metric);
    }

    @Override
    public PATH_METRIC getPathMetric() {
        return tm.getPathMetric();
    }


    @Override
    public void setMaxPathsToCompute(int max) {
        tm.setMaxPathsToCompute(max);
    }

    @Override
    public int getMaxPathsToCompute() {
        return tm.getMaxPathsToCompute();
    }

    @Override
    public Path getPath(DatapathId src, DatapathId dst) {
        return tm.getCurrentTopologyInstance().getPath(src, dst);
    }

    @Override
    public Path getPath(DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort) {
        return tm.getCurrentTopologyInstance().getPath(src, srcPort, dst, dstPort);
    }

    @Override
    public List<Path> getPathsFast(DatapathId src, DatapathId dst) {
        return tm.getCurrentTopologyInstance().getPathsFast(src, dst, tm.getMaxPathsToCompute());
    }

    @Override
    public List<Path> getPathsFast(DatapathId src, DatapathId dst, int numReqPaths) {
        return tm.getCurrentTopologyInstance().getPathsFast(src, dst, numReqPaths);
    }

    @Override
    public List<Path> getPathsSlow(DatapathId src, DatapathId dst, int numReqPaths) {
        return tm.getCurrentTopologyInstance().getPathsSlow(src, dst, numReqPaths);
    }

    @Override
    public Path getSecurePath(Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort) {
        DTSP dtsp = dtspService.getDTSPForService(service);
        List<Path> allPaths = getPathsSlow(src, dst, Integer.MAX_VALUE);
        Risks risks = calculateRisks(service);
        Map<SecurityDimension, Float> acceptableRisks = risks.getAcceptableRisks();
        Map<SecurityDimension, Float> maxRisks = risks.getMaxRisks();
        Path path = null;
        Path rarBfPath = null;
        double rarBfPathDistance = Float.MAX_VALUE;
        Path rarRfPath = null;
        double rarRfPathDistance = Float.MAX_VALUE;
        for(Path p: allPaths) {
            Map<SecurityDimension, Float> pathProperties = pathPropertiesService.calculatePathProperties(p);
            Map<SecurityDimension, Float> pathRisks = riskService.calculateRisk(pathProperties, dtsp.getConsequences());
            if(isPathRiskInRange(acceptableRisks, pathRisks)) {
                if(rarBfPath == null) {
                    rarBfPath = p;
                    rarBfPathDistance = Math.sqrt(Math.pow(pathRisks.get(SecurityDimension.CONFIDENTIALITY), 2)
                            + Math.pow(pathRisks.get(SecurityDimension.INTEGRITY), 2)
                            + Math.pow(pathRisks.get(SecurityDimension.AVAILABILITY), 2)
                            + Math.pow(pathRisks.get(SecurityDimension.TRUST), 2));
                } else {
                    double pathDistance = Math.sqrt(Math.pow(pathRisks.get(SecurityDimension.CONFIDENTIALITY), 2)
                            + Math.pow(pathRisks.get(SecurityDimension.INTEGRITY), 2)
                            + Math.pow(pathRisks.get(SecurityDimension.AVAILABILITY), 2)
                            + Math.pow(pathRisks.get(SecurityDimension.TRUST), 2));
                    if(pathDistance < rarBfPathDistance) {
                        rarBfPath = p;
                        rarBfPathDistance = pathDistance;
                    }
                }
            } else {
                if(rarBfPath == null) {
                    if(isPathRiskInRange(maxRisks, pathRisks)) {
                        float pathConfidentialityRisk = pathRisks.get(SecurityDimension.CONFIDENTIALITY);
                        float confidentialityDifference = pathConfidentialityRisk - acceptableRisks.get(SecurityDimension.CONFIDENTIALITY);
                        if(confidentialityDifference < 0) {
                            confidentialityDifference = 0;
                        }

                        float pathIntegrityRisk = pathRisks.get(SecurityDimension.INTEGRITY);
                        float integrityDifference = pathIntegrityRisk - acceptableRisks.get(SecurityDimension.INTEGRITY);
                        if(integrityDifference < 0) {
                            integrityDifference = 0;
                        }

                        float pathAvailabilityRisk = pathRisks.get(SecurityDimension.AVAILABILITY);
                        float availabilityDifference = pathAvailabilityRisk - acceptableRisks.get(SecurityDimension.AVAILABILITY);
                        if(availabilityDifference < 0) {
                            availabilityDifference = 0;
                        }

                        float pathTrustRisk = pathRisks.get(SecurityDimension.TRUST);
                        float trustDifference = pathTrustRisk - acceptableRisks.get(SecurityDimension.TRUST);
                        if(trustDifference < 0) {
                            trustDifference = 0;
                        }

                        double pathDistance = Math.sqrt(Math.pow(confidentialityDifference, 2)
                                + Math.pow(integrityDifference, 2)
                                + Math.pow(availabilityDifference, 2)
                                + Math.pow(trustDifference, 2));
                        if(pathDistance < rarRfPathDistance) {
                            rarRfPath = p;
                            rarRfPathDistance = pathDistance;
                        }
                    }
                }
            }
        }

        if(rarBfPath != null) {
            path = addSrcAndDstToPath(srcPort, src, dstPort, dst, rarBfPath);
            log.info("Path {} in RAR-BF with distance {}", path, rarBfPathDistance);
            secureRoutingStatisticsService.getSecureRoutingStatistics().setRealizedRequests(secureRoutingStatisticsService.getSecureRoutingStatistics().getRealizedRequests() + 1);
            log.debug("Updating realized requests statistic");
        } else if(rarRfPath != null) {
            path = addSrcAndDstToPath(srcPort, src, dstPort, dst, rarRfPath);
            log.info("Path {} in RAR-RF with distance {}", path, rarRfPathDistance);
            secureRoutingStatisticsService.getSecureRoutingStatistics().setRealizedRequests(secureRoutingStatisticsService.getSecureRoutingStatistics().getRealizedRequests() + 1);
            log.debug("Updating realized requests statistic");
        }

        return path;
    }

    @Override
    public boolean pathExists(DatapathId src, DatapathId dst) {
        return tm.getCurrentTopologyInstance().pathExists(src, dst);
    }
    
    @Override
    public boolean forceRecompute() {
        return tm.forceRecompute();
    }

    /** 
     * Registers an IRoutingDecisionChangedListener.
     *   
     * @param listener
     * @return 
     */
    @Override
    public void addRoutingDecisionChangedListener(IRoutingDecisionChangedListener listener) {
        decisionChangedListeners.add(listener);
    }
    
    /** 
     * Deletes an IRoutingDecisionChangedListener.
     *   
     * @param listener 
     * @return
     */
    @Override
    public void removeRoutingDecisionChangedListener(IRoutingDecisionChangedListener listener) {
        decisionChangedListeners.remove(listener);
    }

    /** 
     * Listens for the event to the IRoutingDecisionChanged listener and calls routingDecisionChanged().
     *   
     * @param changedDecisions
     * @return
     */
    @Override
    public void handleRoutingDecisionChange(Iterable<Masked<U64>> changedDecisions) {
        for (IRoutingDecisionChangedListener listener : decisionChangedListeners) {
            listener.routingDecisionChanged(changedDecisions);
        }
    }

    @Override
    public void enableL3Routing() {
        enableL3RoutingService = true;
    }

    @Override
    public void disableL3Routing() {
        enableL3RoutingService = false;
    }

    @Override
    public boolean isL3RoutingEnabled() {
        return enableL3RoutingService;
    }

    private Path addSrcAndDstToPath(OFPort srcPort, DatapathId srcSw, OFPort dstPort, DatapathId dstSw, Path p) {
        Path path;
        List<NodePortTuple> nptList = new ArrayList<NodePortTuple>(p.getPath());
        NodePortTuple npt = new NodePortTuple(srcSw, srcPort);
        nptList.add(0, npt); // add src port to the front
        npt = new NodePortTuple(dstSw, dstPort);
        nptList.add(npt); // add dst port to the end

        PathId id = new PathId(srcSw, dstSw);
        path = new Path(id, nptList);
        return path;
    }

    private boolean isPathRiskInRange(Map<SecurityDimension, Float> range, Map<SecurityDimension, Float> pathRisks) {
        if(range.get(SecurityDimension.CONFIDENTIALITY) <= pathRisks.get(SecurityDimension.CONFIDENTIALITY)) {
            return false;
        }

        if(range.get(SecurityDimension.INTEGRITY) <= pathRisks.get(SecurityDimension.INTEGRITY)) {
            return false;
        }

        if(range.get(SecurityDimension.AVAILABILITY) <= pathRisks.get(SecurityDimension.AVAILABILITY)) {
            return false;
        }

        if(range.get(SecurityDimension.TRUST) <= pathRisks.get(SecurityDimension.TRUST)) {
            return false;
        }

        return true;
    }

    private Risks calculateRisks(Service service) {
        DTSP dtsp = dtspService.getDTSPForService(service);
        Map<SecurityDimension, Float> acceptableRisks = riskService.calculateRisk(dtsp.getRequirements(), dtsp.getConsequences());
        Map<SecurityDimension, Float> maxRisks = new HashMap<>();
        Map<SecurityDimension, Float> increase = dtsp.getAcceptableRiskIncrease();
        maxRisks.put(SecurityDimension.CONFIDENTIALITY, acceptableRisks.get(SecurityDimension.CONFIDENTIALITY) + acceptableRisks.get(SecurityDimension.CONFIDENTIALITY) * (increase.get(SecurityDimension.CONFIDENTIALITY) / 100.0f));
        maxRisks.put(SecurityDimension.INTEGRITY, acceptableRisks.get(SecurityDimension.INTEGRITY) + acceptableRisks.get(SecurityDimension.INTEGRITY) * (increase.get(SecurityDimension.INTEGRITY) / 100.0f));
        maxRisks.put(SecurityDimension.AVAILABILITY, acceptableRisks.get(SecurityDimension.AVAILABILITY) + acceptableRisks.get(SecurityDimension.AVAILABILITY) * (increase.get(SecurityDimension.AVAILABILITY) / 100.0f));
        maxRisks.put(SecurityDimension.TRUST, acceptableRisks.get(SecurityDimension.TRUST) + acceptableRisks.get(SecurityDimension.TRUST) * (increase.get(SecurityDimension.TRUST) / 100.0f));
        log.debug("Acceptable risks for service {} are {}", service, acceptableRisks);
        log.debug("Max risks for service {} are {}", service, maxRisks);
        return new Risks(acceptableRisks, maxRisks);
    }
}