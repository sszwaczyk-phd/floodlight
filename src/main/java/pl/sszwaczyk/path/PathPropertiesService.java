package pl.sszwaczyk.path;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.routing.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.path.calculator.MinPathPropertiesCalculator;
import pl.sszwaczyk.path.calculator.MultiplicationPathPropertiesCalculator;
import pl.sszwaczyk.path.calculator.PathPropertiesCalculator;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.*;

public class PathPropertiesService implements IFloodlightModule, IPathPropertiesService {

    protected static final Logger log = LoggerFactory.getLogger(PathPropertiesService.class);

    private IOFSwitchService switchService;
    private ILinkDiscoveryService linkService;

    private PathPropertiesCalculator calculator;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(IPathPropertiesService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IPathPropertiesService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IOFSwitchService.class);
        l.add(ILinkDiscoveryService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        this.switchService = context.getServiceImpl(IOFSwitchService.class);
        this.linkService = context.getServiceImpl(ILinkDiscoveryService.class);

        Map<String, String> configParameters = context.getConfigParams(this);
        String calcString = configParameters.get("path-properties-calculator");
        if(calcString == null || calcString.isEmpty()) {
            throw new FloodlightModuleException("Cannot init PathPropertiesService because PathPropertiesCalculator not specified!");
        }

        if(calcString.equals("minimum")) {
            calculator = new MinPathPropertiesCalculator(switchService, linkService);
        } else if(calcString.equals("multiplication")) {
            calculator = new MultiplicationPathPropertiesCalculator(switchService, linkService);
        } else {
            throw new FloodlightModuleException("Cannot init PathPropertiesService because wrong PathPropertiesCalculator specified (" + calcString + ")!");
        }
        log.info("PathPropertiesService initialized with " + calcString + " path properties calculator.");
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {

    }

    @Override
    public Map<SecurityDimension, Float> calculatePathProperties(Path path) {
        Map<SecurityDimension, Float> pathProperties = calculator.calculatePathProperties(path);
        log.debug("Path properties {} for Path {}", pathProperties, path);
        return pathProperties;
    }
}
