package pl.sszwaczyk.routing;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.RoutingManager;
import net.floodlightcontroller.statistics.IStatisticsService;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.python.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.path.IPathPropertiesService;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.routing.solver.KShortestPathSolver;
import pl.sszwaczyk.routing.solver.Solver;
import pl.sszwaczyk.security.dtsp.IDTSPService;
import pl.sszwaczyk.security.risk.IRiskCalculationService;
import pl.sszwaczyk.service.IServiceService;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.statistics.ISecureRoutingStatisticsService;
import pl.sszwaczyk.uneven.IUnevenService;
import pl.sszwaczyk.uneven.UnevenMetric;
import pl.sszwaczyk.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SecureRoutingManager extends RoutingManager implements ISecureRoutingService {

    private Logger log = LoggerFactory.getLogger(SecureRoutingManager.class);

    //Secure routing
    private IRiskCalculationService riskService;
    private IDTSPService dtspService;
    private IPathPropertiesService pathPropertiesService;
    private IStatisticsService statisticsService;
    private ISecureRoutingStatisticsService secureRoutingStatisticsService;
    private IUnevenService unevenService;

    //Solver
    private Solver solver;

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
        l.add(IStatisticsService.class);
        l.add(IUnevenService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        super.init(context);
        riskService = context.getServiceImpl(IRiskCalculationService.class);
        dtspService = context.getServiceImpl(IDTSPService.class);
        pathPropertiesService = context.getServiceImpl(IPathPropertiesService.class);
        secureRoutingStatisticsService = context.getServiceImpl(ISecureRoutingStatisticsService.class);
        statisticsService = context.getServiceImpl(IStatisticsService.class);
        unevenService = context.getServiceImpl(IUnevenService.class);

        Map<String, String> configParameters = context.getConfigParams(this);
        String stringSolver = configParameters.get("solver");
        if(stringSolver == null || stringSolver.isEmpty()) {
            throw new FloodlightModuleException("Solver not configured!");
        }
        if(stringSolver.equals("k-shortest")) {
            log.info("Configured to using K-shortest-paths solver");
            String kString = configParameters.get("k");
            int k = 0;
            if(kString == null) {
                k = Integer.MAX_VALUE;
                log.info("K shortest path not set. Default to " + Integer.MAX_VALUE);
            } else {
                k = Integer.valueOf(kString);
                log.info("K shortest path set to " + k);
            }
            String unevenMetricString = configParameters.get("uneven-metric");
            UnevenMetric unevenMetric;
            if(unevenMetricString == null) {
                unevenMetric = UnevenMetric.VARIATION_COEFFICIENT;
                log.info("Uneven metric not set. Default to " + unevenMetric);
            } else {
                if(unevenMetricString.equals("gap")) {
                    unevenMetric = UnevenMetric.GAP;
                } else if(unevenMetricString.equals("variance")) {
                    unevenMetric = UnevenMetric.VARIANCE;
                } else if(unevenMetricString.equals("variation-coefficient")) {
                    unevenMetric = UnevenMetric.VARIATION_COEFFICIENT;
                } else {
                    throw new FloodlightModuleException("Not recognized uneven metric set " + unevenMetricString);
                }
                log.info("Uneven metric set to " + unevenMetric);
            }
            solver = KShortestPathSolver.builder()
                    .routingService(this)
                    .riskService(riskService)
                    .dtspService(dtspService)
                    .pathPropertiesService(pathPropertiesService)
                    .statisticsService(statisticsService)
                    .unevenService(unevenService)
                    .k(k)
                    .unevenMetric(unevenMetric)
                    .build();

        } else {
            throw new FloodlightModuleException("Unrecognized solver configured");
        }

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        super.startUp(context);
    }

    @Override
    public Path getSecurePath(User user, Service service, DatapathId src, OFPort srcPort, DatapathId dst, OFPort dstPort) {
        long start = System.currentTimeMillis();
        Decision decision = solver.solve(user, service, src, srcPort, dst, dstPort);
        decision.setTime(System.currentTimeMillis() - start);

        secureRoutingStatisticsService.getSecureRoutingStatistics().addDecision(decision);
        //update stats base on solve decision
//        if(!decision.isSolved()) {
//            log.info("Updating not realized requests statistic");
//            secureRoutingStatisticsService.getSecureRoutingStatistics().updateNotRealized(user, service);
//            return new Path(null, ImmutableList.of());
//        } else if(decision.getRegion().equals(SolveRegion.RAR_BF)) {
//            log.info("Path {} in RAR-BF with distance {}", decision.getPath(), decision.getValue());
//            log.debug("Updating realized requests statistic");
//            secureRoutingStatisticsService.getSecureRoutingStatistics().updateRealized(user, service, decision);
//        } else if(decision.getRegion().equals(SolveRegion.RAR_RF)) {
//            log.info("Path {} in RAR-RF with distance {}", decision.getPath(), decision.getValue());
//            log.debug("Updating realized requests statistic");
//            secureRoutingStatisticsService.getSecureRoutingStatistics().updateRealized(user, service, decision);
//        }

        if(!decision.isSolved()) {
            return new Path(null, ImmutableList.of());
        }

        return decision.getPath();
    }


}
