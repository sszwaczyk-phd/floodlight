package pl.sszwaczyk.routing;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;
import net.floodlightcontroller.routing.RoutingManager;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.path.IPathPropertiesService;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.dtsp.DTSP;
import pl.sszwaczyk.security.dtsp.IDTSPService;
import pl.sszwaczyk.security.risk.IRiskCalculationService;
import pl.sszwaczyk.security.risk.Risks;
import pl.sszwaczyk.service.IServiceService;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.statistics.ISecureRoutingStatisticsService;

import java.util.*;

public class SecureRoutingManager extends RoutingManager implements ISecureRoutingService {

    private Logger log = LoggerFactory.getLogger(SecureRoutingManager.class);

    //Secure routing
    private IRiskCalculationService riskService;
    private IDTSPService dtspService;
    private IPathPropertiesService pathPropertiesService;
    private ISecureRoutingStatisticsService secureRoutingStatisticsService;


    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(ISecureRoutingService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(ISecureRoutingService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = super.getModuleDependencies();
        l.add(IServiceService.class);
        l.add(IDTSPService.class);
        l.add(IRiskCalculationService.class);
        l.add(IPathPropertiesService.class);
        l.add(ISecureRoutingStatisticsService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        super.init(context);
        riskService = context.getServiceImpl(IRiskCalculationService.class);
        dtspService = context.getServiceImpl(IDTSPService.class);
        pathPropertiesService = context.getServiceImpl(IPathPropertiesService.class);
        secureRoutingStatisticsService = context.getServiceImpl(ISecureRoutingStatisticsService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        super.startUp(context);
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
