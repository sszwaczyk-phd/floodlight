package pl.sszwaczyk.routing.solver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.path.IPathPropertiesService;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.dtsp.DTSP;
import pl.sszwaczyk.security.dtsp.IDTSPService;
import pl.sszwaczyk.security.risk.IRiskCalculationService;
import pl.sszwaczyk.security.risk.Risks;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.uneven.IUnevenService;
import pl.sszwaczyk.uneven.UnevenMetric;
import pl.sszwaczyk.user.User;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
public class KShortestPathSolver implements Solver {

    private static Logger log = LoggerFactory.getLogger(KShortestPathSolver.class);

    private IRoutingService routingService;
    private IRiskCalculationService riskService;
    private IDTSPService dtspService;
    private IPathPropertiesService pathPropertiesService;
    private IStatisticsService statisticsService;
    private IUnevenService unevenService;

    private int k;
    private boolean chooseMinUneven;
    private UnevenMetric unevenMetric;

    @Override
    public Decision solve(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort) {
        DTSP dtsp = dtspService.getDTSPForService(service);
        Risks risks = calculateRisks(service);
        Map<SecurityDimension, Float> acceptableRisks = risks.getAcceptableRisks();
        Map<SecurityDimension, Float> maxRisks = risks.getMaxRisks();
        Map<NodePortTuple, SwitchPortBandwidth> actualBandwidthConsumption = statisticsService.getBandwidthConsumption();
        Double unevenBefore = unevenService.getUneven(unevenMetric);
        Double unevenAfter = Double.MAX_VALUE;

        Path path = null;
        Path rarBfPath = null;
        double rarBfPathDistance = Float.MAX_VALUE;
        Map<SecurityDimension, Float> rarBfPathRisks = null;
        Path rarRfPath = null;
        double rarRfPathDistance = Float.MAX_VALUE;
        Map<SecurityDimension, Float> rarRfPathRisks = null;
        Reason reason = null;

        int lastSize = 0;
        boolean wasPathChecked = false;
        while(rarBfPath == null && rarRfPath == null) {
            List<Path> paths = routingService.getPathsSlow(src, dst, k + lastSize);
            if(paths.size() <= lastSize) {
                log.error("Break");
                break;
            }
            log.info("Searching shortests paths between " + (lastSize + 1) + " and " + paths.size());
            int pathsSize = paths.size();
            paths = paths.subList(lastSize, paths.size());
            lastSize = pathsSize;


            reason = null;
            List<Path> filteredPaths = filterBandwidth(paths, dtsp.getService().getBandwidth());
            if(filteredPaths.size() == 0) {
                if(!wasPathChecked) {
                    reason = Reason.CANNOT_FULFILL_BANDWIDTH;
                }
                log.info("No path which fulfill bandwidth requirement.");
                continue;
            }

            filteredPaths = filterLatency(filteredPaths, dtsp.getService().getMaxLatency());
            if(filteredPaths.size() == 0) {
                if(!wasPathChecked) {
                    reason = Reason.CANNOT_FULFILL_LATENCY;
                }
                log.info("No path which fulfill latency requirement.");
                continue;
            }

            log.info("Filtered size = " + filteredPaths.size());

            for(Path p: filteredPaths) {
                wasPathChecked = true;
                reason = Reason.CANNOT_FULFILL_DTSP;
                log.info("Checking path " + p);

                Map<SecurityDimension, Float> pathProperties = pathPropertiesService.calculatePathProperties(p);
                Map<SecurityDimension, Float> pathRisks = riskService.calculateRisk(pathProperties, dtsp.getConsequences());

                Map<NodePortTuple, SwitchPortBandwidth> predicateBandwidthConsumption = new HashMap<>();
                List<NodePortTuple> npts = p.getPath();
                actualBandwidthConsumption.forEach((nodePortTuple, switchPortBandwidth) -> {
                    if(npts.contains(nodePortTuple)) {
                        predicateBandwidthConsumption.put(nodePortTuple, SwitchPortBandwidth.of(switchPortBandwidth.getSwitchId(),
                                switchPortBandwidth.getSwitchPort(),
                                switchPortBandwidth.getLinkSpeedBitsPerSec(),
                                switchPortBandwidth.getBitsPerSecondRx(),
                                U64.of(switchPortBandwidth.getBitsPerSecondTx().getValue() + dtsp.getService().getBandwidth().longValue()),
                                switchPortBandwidth.getPriorByteValueRx(),
                                switchPortBandwidth.getPriorByteValueTx()));
                    } else {
                        //copy actual
                        predicateBandwidthConsumption.put(nodePortTuple, SwitchPortBandwidth.of(switchPortBandwidth.getSwitchId(),
                                switchPortBandwidth.getSwitchPort(),
                                switchPortBandwidth.getLinkSpeedBitsPerSec(),
                                switchPortBandwidth.getBitsPerSecondRx(),
                                switchPortBandwidth.getBitsPerSecondTx(),
                                switchPortBandwidth.getPriorByteValueRx(),
                                switchPortBandwidth.getPriorByteValueTx()));
                    }
                });
                Double pathUnevenAfter = unevenService.getUneven(unevenMetric, predicateBandwidthConsumption);
                long latency = p.getLatency().getValue();
                log.debug("Uneven after = " + pathUnevenAfter + " and latency = " + latency);

                if(isPathRiskInRange(acceptableRisks, pathRisks)) {
                    if(rarBfPath == null) {
                        rarBfPath = p;
                        rarBfPathDistance = Math.sqrt(Math.pow(pathRisks.get(SecurityDimension.CONFIDENTIALITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.INTEGRITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.AVAILABILITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.TRUST), 2));
                        rarBfPathRisks = pathRisks;
                        unevenAfter = pathUnevenAfter;
                    } else {
                        double pathDistance = Math.sqrt(Math.pow(pathRisks.get(SecurityDimension.CONFIDENTIALITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.INTEGRITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.AVAILABILITY), 2)
                                + Math.pow(pathRisks.get(SecurityDimension.TRUST), 2));
                        if(chooseMinUneven) {
                            if(pathUnevenAfter < unevenAfter) {
                                rarBfPath = p;
                                rarBfPathDistance = pathDistance;
                                rarBfPathRisks = pathRisks;
                                unevenAfter = pathUnevenAfter;
                            }
                        } else {
                            if(pathDistance < rarBfPathDistance) {
                                rarBfPath = p;
                                rarBfPathDistance = pathDistance;
                                rarBfPathRisks = pathRisks;
                                unevenAfter = pathUnevenAfter;
                            }
                        }
                    }
                }

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
                            unevenAfter = pathUnevenAfter;
                        }
                    }
                }

            }
        }

        if(rarBfPath == null && rarRfPath == null) {
            log.info("Cannot find path to realize service " + service.getId());
            if(reason == null) {
                reason = Reason.CANNOT_FULFILL_DTSP;
            }
            return Decision.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .service(service)
                    .acceptableRisks(acceptableRisks)
                    .maxRisks(maxRisks)
                    .solved(false)
                    .reason(reason)
                    .date(LocalTime.now())
                    .build();
        }

        if(rarBfPath != null) {
            path = addSrcAndDstToPath(srcPort, src, dstPort, dst, rarBfPath);
            log.info("Path {} in RAR-BF with distance {}", path, rarBfPathDistance);
            return Decision.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .service(service)
                    .acceptableRisks(acceptableRisks)
                    .maxRisks(maxRisks)
                    .solved(true)
                    .unevenBefore(unevenBefore)
                    .unevenAfter(unevenAfter)
                    .region(SolveRegion.RAR_BF)
                    .value(rarBfPathDistance)
                    .risks(rarBfPathRisks)
                    .risk(aggregateRisk(rarBfPathRisks))
                    .date(LocalTime.now())
                    .path(path)
                    .pathLength(path.getHopCount())
                    .pathLatency(path.getLatency().getValue())
                    .build();
        } else {
            path = addSrcAndDstToPath(srcPort, src, dstPort, dst, rarRfPath);
            log.info("Path {} in RAR-RF with distance {}", path, rarRfPathDistance);
            return Decision.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .service(service)
                    .acceptableRisks(acceptableRisks)
                    .maxRisks(maxRisks)
                    .solved(true)
                    .unevenBefore(unevenBefore)
                    .unevenAfter(unevenAfter)
                    .region(SolveRegion.RAR_RF)
                    .value(rarRfPathDistance)
                    .risks(rarRfPathRisks)
                    .risk(aggregateRisk(rarRfPathRisks))
                    .date(LocalTime.now())
                    .path(path)
                    .pathLength(path.getHopCount())
                    .pathLatency(path.getLatency().getValue())
                    .build();
        }

    }

    @Override
    public Decision solveShortest(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort) {
        DTSP dtsp = dtspService.getDTSPForService(service);
        Risks risks = calculateRisks(service);
        Map<SecurityDimension, Float> acceptableRisks = risks.getAcceptableRisks();
        Map<SecurityDimension, Float> maxRisks = risks.getMaxRisks();
        Map<NodePortTuple, SwitchPortBandwidth> actualBandwidthConsumption = statisticsService.getBandwidthConsumption();
        Double unevenBefore = unevenService.getUneven(unevenMetric);
        Double unevenAfter = Double.MAX_VALUE;

        List<Path> paths = routingService.getPathsSlow(src, dst, 1);
        Path path = paths.get(0);

        Map<SecurityDimension, Float> pathProperties = pathPropertiesService.calculatePathProperties(path);
        Map<SecurityDimension, Float> pathRisks = riskService.calculateRisk(pathProperties, dtsp.getConsequences());

        Map<NodePortTuple, SwitchPortBandwidth> predicateBandwidthConsumption = new HashMap<>();
        List<NodePortTuple> npts = path.getPath();
        actualBandwidthConsumption.forEach((nodePortTuple, switchPortBandwidth) -> {
            if(npts.contains(nodePortTuple)) {
                predicateBandwidthConsumption.put(nodePortTuple, SwitchPortBandwidth.of(switchPortBandwidth.getSwitchId(),
                        switchPortBandwidth.getSwitchPort(),
                        switchPortBandwidth.getLinkSpeedBitsPerSec(),
                        switchPortBandwidth.getBitsPerSecondRx(),
                        U64.of(switchPortBandwidth.getBitsPerSecondTx().getValue() + dtsp.getService().getBandwidth().longValue()),
                        switchPortBandwidth.getPriorByteValueRx(),
                        switchPortBandwidth.getPriorByteValueTx()));
            } else {
                //copy actual
                predicateBandwidthConsumption.put(nodePortTuple, SwitchPortBandwidth.of(switchPortBandwidth.getSwitchId(),
                        switchPortBandwidth.getSwitchPort(),
                        switchPortBandwidth.getLinkSpeedBitsPerSec(),
                        switchPortBandwidth.getBitsPerSecondRx(),
                        switchPortBandwidth.getBitsPerSecondTx(),
                        switchPortBandwidth.getPriorByteValueRx(),
                        switchPortBandwidth.getPriorByteValueTx()));
            }
        });
        Double pathUnevenAfter = unevenService.getUneven(unevenMetric, predicateBandwidthConsumption);
        long latency = path.getLatency().getValue();
        log.debug("Uneven after = " + pathUnevenAfter + " and latency = " + latency);

        boolean solved = isPathRiskInRange(acceptableRisks, pathRisks);
        if(solved == false) {
            log.info("Cannot find path to realize service " + service.getId());
            return Decision.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .service(service)
                    .acceptableRisks(acceptableRisks)
                    .maxRisks(maxRisks)
                    .solved(false)
                    .reason(Reason.CANNOT_FULFILL_DTSP)
                    .date(LocalTime.now())
                    .build();
        } else {
            double rarBfPathDistance = Math.sqrt(Math.pow(pathRisks.get(SecurityDimension.CONFIDENTIALITY), 2)
                    + Math.pow(pathRisks.get(SecurityDimension.INTEGRITY), 2)
                    + Math.pow(pathRisks.get(SecurityDimension.AVAILABILITY), 2)
                    + Math.pow(pathRisks.get(SecurityDimension.TRUST), 2));
            path = addSrcAndDstToPath(srcPort, src, dstPort, dst, path);
            log.info("Path {} in RAR-BF with distance {}", path, rarBfPathDistance);
            return Decision.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .service(service)
                    .acceptableRisks(acceptableRisks)
                    .maxRisks(maxRisks)
                    .solved(true)
                    .unevenBefore(unevenBefore)
                    .unevenAfter(unevenAfter)
                    .region(SolveRegion.RAR_BF)
                    .value(rarBfPathDistance)
                    .risks(pathRisks)
                    .risk(aggregateRisk(pathRisks))
                    .date(LocalTime.now())
                    .path(path)
                    .pathLength(path.getHopCount())
                    .pathLatency(path.getLatency().getValue())
                    .build();
        }

    }

    private List<Path> filterLatency(List<Path> paths, Long maxLatency) {
        return paths.stream().filter(path -> {
            if(path.getLatency().getValue() > maxLatency) {
                log.info("Filtering path " + path.toString() + " because of Latency. Path Latency = " + path.getLatency().getValue() + " ms");
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private List<Path> filterBandwidth(List<Path> paths, Double bandwidth) {
        return paths.stream().filter(path -> {
            for(NodePortTuple npt: path.getPath()) {
                SwitchPortBandwidth bandwidthConsumption = statisticsService.getBandwidthConsumption(npt.getNodeId(), npt.getPortId());
                if((bandwidthConsumption.getAvailableTxBandwidth() * 1000) < bandwidth) {
                    log.info("Filtering path " + path.toString() + " because of Bandwidth. Current Path available bandwidth = " + (bandwidthConsumption.getAvailableTxBandwidth() * 1000) + " b/s");
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    private float aggregateRisk(Map<SecurityDimension, Float> rarBfPathRisks) {
        float sum = 0.0f;
        for(Float r: rarBfPathRisks.values()) {
            sum += r;
        }
        return sum;
    }

    private Path addSrcAndDstToPath(OFPort srcPort, DatapathId srcSw, OFPort dstPort, DatapathId dstSw, Path p) {
        List<NodePortTuple> nptList = p.getPath();
        NodePortTuple npt = new NodePortTuple(srcSw, srcPort);
        nptList.add(0, npt); // add src port to the front
        npt = new NodePortTuple(dstSw, dstPort);
        nptList.add(npt); // add dst port to the end

        PathId id = new PathId(srcSw, dstSw);
        p.setId(id);
        return p;
    }

    private boolean isPathRiskInRange(Map<SecurityDimension, Float> range, Map<SecurityDimension, Float> pathRisks) {
        if(range.get(SecurityDimension.CONFIDENTIALITY) < pathRisks.get(SecurityDimension.CONFIDENTIALITY)) {
            return false;
        }

        if(range.get(SecurityDimension.INTEGRITY) < pathRisks.get(SecurityDimension.INTEGRITY)) {
            return false;
        }

        if(range.get(SecurityDimension.AVAILABILITY) < pathRisks.get(SecurityDimension.AVAILABILITY)) {
            return false;
        }

        if(range.get(SecurityDimension.TRUST) < pathRisks.get(SecurityDimension.TRUST)) {
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
