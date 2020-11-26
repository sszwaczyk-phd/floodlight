package pl.sszwaczyk.repository.link;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.repository.link.web.LinkStatisticsRepositoryRoutable;
import pl.sszwaczyk.uneven.IUnevenService;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LinkStatisticsRepository implements IFloodlightModule, ILinkStatisticsRepository {

    private Logger log = LoggerFactory.getLogger(LinkStatisticsRepository.class);

    private List<MaxLinkUtilization> maxBandwidthConsumption = new ArrayList<>();
    private Map<UnevenMetric, Double> maxUneven = new HashMap<>();

    private final List<LinkUtilizationAtTime> linkUtilizationAtTimes = new ArrayList<>();

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
        threadPoolService.getScheduledExecutor().scheduleAtFixedRate(new LinkUtilizationAtTimeFetcher(), 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public List<MaxLinkUtilization> getMaxLinksBandwidth() {
        return Collections.unmodifiableList(maxBandwidthConsumption);
    }

    @Override
    public Map<UnevenMetric, Double> getMaxUneven() {
        return Collections.unmodifiableMap(maxUneven);
    }

    @Override
    public List<LinkUtilizationAtTime> getLinkUtilizationAtTimes() {
        return Collections.unmodifiableList(linkUtilizationAtTimes);
    }

    class MaxStatisticsFetcher implements Runnable {

        @Override
        public void run() {
            Map<NodePortTuple, SwitchPortBandwidth> bandwidthConsumption = statisticsService.getBandwidthConsumption();
            for(SwitchPortBandwidth spb: bandwidthConsumption.values()) {
                MaxLinkUtilization max = null;
                for(MaxLinkUtilization maxLinkUtilization: maxBandwidthConsumption) {
                    if(maxLinkUtilization.getId().equals(spb.getSwitchId()) && maxLinkUtilization.getPt().getPortNumber() == spb.getSwitchPort().getPortNumber()) {
                        max = maxLinkUtilization;
                    }
                }
                if(max == null) {
                    maxBandwidthConsumption.add(new MaxLinkUtilization(spb.getSwitchId(), spb.getSwitchPort(), spb.getRxUtilization(), spb.getRxUtilizationPercent(), spb.getTxUtilization(), spb.getTxUtilizationPercent()));
                } else {
                    if(spb.getRxUtilization() > max.getMaxRxUtilization()) {
                        max.setMaxRxUtilization(spb.getRxUtilization());
                        max.setMaxRxUtilizationPercent(spb.getRxUtilizationPercent());
                    }
                    if(spb.getTxUtilization() > max.getMaxTxUtilization()) {
                        max.setMaxTxUtilization(spb.getTxUtilization());
                        max.setMaxTxUtilizationPercent(spb.getTxUtilizationPercent());
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

    class LinkUtilizationAtTimeFetcher implements Runnable {

        @Override
        public void run() {
            log.debug("Fetching actual link utilization stats...");
            LocalTime date = LocalTime.now();

            Map<NodePortTuple, SwitchPortBandwidth> bandwidthConsumption = statisticsService.getBandwidthConsumption();
            for (SwitchPortBandwidth spb: bandwidthConsumption.values()) {
                if (spb.getSwitchPort().getPortNumber() > 0) {
                    LinkUtilizationAtTime linkUtilizationAtTime = LinkUtilizationAtTime.builder()
                            .date(date)
                            .datapathId(spb.getSwitchId())
                            .pt(spb.getSwitchPort())
                            .rxUtilization(spb.getRxUtilization())
                            .rxUtilizationPercent(spb.getRxUtilizationPercent())
                            .txUtilization(spb.getTxUtilization())
                            .txUtilizationPercent(spb.getTxUtilizationPercent())
                            .build();
                    linkUtilizationAtTimes.add(linkUtilizationAtTime);
                    log.debug("Added link utilization: " + linkUtilizationAtTime + " to repository");
                }

            }
            log.debug("Fetching actual link utilization stats..");
        }

    }
}
