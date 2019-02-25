package pl.sszwaczyk.uneven;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.statistics.ISecureRoutingStatisticsService;
import pl.sszwaczyk.uneven.calculator.GapUnevenCalculator;
import pl.sszwaczyk.uneven.web.UnevenRoutable;

import java.util.*;

public class UnevenService implements IFloodlightModule, IUnevenService {

    private Logger log = LoggerFactory.getLogger(IUnevenService.class);

    private IRestApiService restApiService;
    private IStatisticsService statisticsService;

    private GapUnevenCalculator gapUnevenCalculator;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(IUnevenService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IUnevenService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IRestApiService.class);
        l.add(IStatisticsService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);
        statisticsService = context.getServiceImpl(IStatisticsService.class);

        gapUnevenCalculator = new GapUnevenCalculator();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new UnevenRoutable());
    }

    @Override
    public Map<UnevenMetric, Double> getUneven() {
        log.debug("Calculating uneven use of resources...");
        Map<UnevenMetric, Double> unevens = new HashMap<>();

        Map<NodePortTuple, SwitchPortBandwidth> bandwidthConsumption = statisticsService.getBandwidthConsumption();

        unevens.put(gapUnevenCalculator.getMetric(), gapUnevenCalculator.calculateUneven(bandwidthConsumption));

        return unevens;
    }

    @Override
    public Double getUneven(UnevenMetric metric) {
        //TODO: implement
        return null;
    }
}
