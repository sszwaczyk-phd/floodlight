package pl.sszwaczyk.statistics;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.statistics.web.SecureRoutingStatisticsRoutable;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class SecureRoutingStatisticsService implements IFloodlightModule, ISecureRoutingStatisticsService {

    private Logger log = LoggerFactory.getLogger(ISecureRoutingStatisticsService.class);

    private IRestApiService restApiService;

    private SecureRoutingStatistics statistics;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(ISecureRoutingStatisticsService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(ISecureRoutingStatisticsService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IRestApiService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);

        statistics = new SecureRoutingStatistics();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new SecureRoutingStatisticsRoutable());
    }


    @Override
    public SecureRoutingStatistics getSecureRoutingStatistics() {
        return statistics;
    }

    @Override
    public String snapshotStatisticsToFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("Realized requests = " + statistics.getRealizedRequests() + "\n");
        sb.append("Not realized requests = " + statistics.getNotRealizedRequests() + "\n");

        String statsFile = "/tmp/" + UUID.randomUUID();
        try (PrintWriter out = new PrintWriter(statsFile)) {
            out.println(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        log.info("Snapshot of secure routing statistics to " + statsFile);
        return statsFile;
    }
}
