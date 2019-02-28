package pl.sszwaczyk.repository;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.repository.web.SecureFlowsRepositoryRoutable;
import pl.sszwaczyk.utils.AddressesAndPorts;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SecureFlowsRepository implements IFloodlightModule, ISecureFlowsRepository {

    private Logger log = LoggerFactory.getLogger(SecureFlowsRepository.class);

    private IRestApiService restApiService;

    private volatile Map<AddressesAndPorts, Path> paths = new ConcurrentHashMap<>();

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(ISecureFlowsRepository.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(ISecureFlowsRepository.class, this);
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
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new SecureFlowsRepositoryRoutable());
    }

    @Override
    public void registerFlow(AddressesAndPorts ap, Path path) {
        paths.put(ap, path);
    }

    @Override
    public Map<AddressesAndPorts, Path> getFlows() {
        return paths;
    }

    @Override
    public void deleteFlow(AddressesAndPorts ap) {
        paths.remove(ap);
    }
}
