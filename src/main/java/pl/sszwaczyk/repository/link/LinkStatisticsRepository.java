package pl.sszwaczyk.repository.link;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.repository.link.web.LinkStatisticsRepositoryRoutable;
import pl.sszwaczyk.uneven.IUnevenService;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LinkStatisticsRepository implements IFloodlightModule, ILinkStatisticsRepository {

    private Logger log = LoggerFactory.getLogger(LinkStatisticsRepository.class);

    private List<SwitchPortBandwidth> maxBandwidthConsumption = new ArrayList<>();
    private Map<UnevenMetric, Double> maxUneven = new HashMap<>();

    private IStatisticsService statisticsService;
    private IUnevenService unevenService;
    private IRestApiService restApiService;
    private IThreadPoolService threadPoolService;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(ILinkStatisticsRepository.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(ILinkStatisticsRepository.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IStatisticsService.class);
        l.add(IUnevenService.class);
        l.add(IRestApiService.class);
        l.add(IThreadPoolService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        statisticsService = context.getServiceImpl(IStatisticsService.class);
        unevenService = context.getServiceImpl(IUnevenService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);
        threadPoolService = context.getServiceImpl(IThreadPoolService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new LinkStatisticsRepositoryRoutable());
        threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new MaxStatisticsFetcher(), 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public List<SwitchPortBandwidth> getMaxLinksBandwidth() {
        return Collections.unmodifiableList(maxBandwidthConsumption);
    }

    @Override
    public Map<UnevenMetric, Double> getMaxUneven() {
        return Collections.unmodifiableMap(maxUneven);
    }

    class MaxStatisticsFetcher implements Runnable {

        @Override
        public void run() {
            Map<NodePortTuple, SwitchPortBandwidth> bandwidthConsumption = statisticsService.getBandwidthConsumption();
            for(SwitchPortBandwidth spb: bandwidthConsumption.values()) {
                int indexOf = maxBandwidthConsumption.indexOf(spb);
                if(indexOf == -1) {
                    log.debug("Max bandwidth consumption set for " + spb);
                    maxBandwidthConsumption.add(spb);
                } else {
                    SwitchPortBandwidth max = maxBandwidthConsumption.get(indexOf);
                    if(spb.getTxUtilization() > max.getTxUtilization()) {
                        maxBandwidthConsumption.remove(max);
                        maxBandwidthConsumption.add(spb);
                        log.debug("Max bandwidth consumption updated for " + max + " to " + spb);
                    }
                }
            }

            Map<UnevenMetric, Double> uneven = unevenService.getUneven(bandwidthConsumption);
            for(UnevenMetric metric: uneven.keySet()) {
                Double result = uneven.get(metric);
                Double max = maxUneven.get(metric);
                if(max == null) {
                    maxUneven.put(metric, result);
                    log.debug("Max uneven set for metric " + metric + " to " + result);
                } else {
                    if(result.isNaN()) {
                        continue;
                    } else if(max.isNaN()) {
                        maxUneven.put(metric, result);
                        log.debug("Max uneven updated for metric " + metric + " from " + max + " to " + result);
                    } else if(result > max){
                        maxUneven.put(metric, result);
                        log.debug("Max uneven updated for metric " + metric + " from " + max + " to " + result);
                    }
                }
            }

        }

    }

}
