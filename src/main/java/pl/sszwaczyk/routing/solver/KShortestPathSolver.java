package pl.sszwaczyk.routing.solver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;
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
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
public class KShortestPathSolver implements Solver {

    private static Logger log = LoggerFactory.getLogger(KShortestPathSolver.class);

    private IRoutingService routingService;
    private IRiskCalculationService riskService;
    private IDTSPService dtspService;
    private IPathPropertiesService pathPropertiesService;

    private int k;

    @Override
    public SolveResult solve(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort) {
        DTSP dtsp = dtspService.getDTSPForService(service);
        Risks risks = calculateRisks(service);
        Map<SecurityDimension, Float> acceptableRisks = risks.getAcceptableRisks();
        Map<SecurityDimension, Float> maxRisks = risks.getMaxRisks();
        Path path = null;
        Path rarBfPath = null;
        double rarBfPathDistance = Float.MAX_VALUE;
        Map<SecurityDimension, Float> rarBfPathRisks = null;
        Path rarRfPath = null;
        double rarRfPathDistance = Float.MAX_VALUE;
        Map<SecurityDimension, Float> rarRfPathRisks = null;

        int lastSize = 0;
        int i = 0;
        while(rarBfPath == null && rarRfPath == null) {
            List<Path> paths = routingService.getPathsSlow(src, dst, k + i);
            if(paths.size() <= lastSize) {
                break;
            }
            for(int j = lastSize; j < paths.size(); j++) {
                log.info("Searching shortests paths between " + j + " and " + (k + i));
                Path p = paths.get(j);
                Map<SecurityDimension, Float> pathProperties = pathPropertiesService.calculatePathProperties(p);
                Map<SecurityDimension, Float> pathRisks = riskService.calculateRisk(pathProperties, dtsp.getConsequences());
                if(isPathRiskInRange(acceptableRisks, pathRisks)) {
                    if(rarBfPath == null) {
                        rarBfPath = p;
                        rarBfPathDistance = Math.sqrt(Math.pow(pathRisks.get(SecurityDimension.CONFIDENTIALITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.INTEGRITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.AVAILABILITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.TRUST), 2));
                        rarBfPathRisks = pathRisks;
                    } else {
                        double pathDistance = Math.sqrt(Math.pow(pathRisks.get(SecurityDimension.CONFIDENTIALITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.INTEGRITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.AVAILABILITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.TRUST), 2));
                        if(pathDistance < rarBfPathDistance) {
                            rarBfPath = p;
                            rarBfPathDistance = pathDistance;
                            rarBfPathRisks = pathRisks;
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
                                rarRfPathRisks = pathRisks;
                            }
                        }
                    }
                }
            }
            lastSize = paths.size();
            i = lastSize;
        }

        if(rarBfPath == null && rarRfPath == null) {
            log.info("Cannot find path to realize service " + service.getId());
            return SolveResult.builder()
                .solved(false)
                .build();
        }

        if(rarBfPath != null) {
            path = addSrcAndDstToPath(srcPort, src, dstPort, dst, rarBfPath);
            log.info("Path {} in RAR-BF with distance {}", path, rarBfPathDistance);
            return SolveResult.builder()
                    .solved(true)
                    .region(SolveRegion.RAR_BF)
                    .path(path)
                    .value(rarBfPathDistance)
                    .risks(rarBfPathRisks)
                    .risk(aggregateRisk(rarBfPathRisks))
                    .build();
        } else {
            path = addSrcAndDstToPath(srcPort, src, dstPort, dst, rarRfPath);
            log.info("Path {} in RAR-RF with distance {}", path, rarRfPathDistance);
            return SolveResult.builder()
                    .solved(true)
                    .region(SolveRegion.RAR_RF)
                    .path(path)
                    .value(rarRfPathDistance)
                    .risks(rarRfPathRisks)
                    .risk(aggregateRisk(rarRfPathRisks))
                    .build();
        }

    }

    private float aggregateRisk(Map<SecurityDimension, Float> rarBfPathRisks) {
        float sum = 0.0f;
        for(Float r: rarBfPathRisks.values()) {
            sum += r;
        }
        return sum;
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
