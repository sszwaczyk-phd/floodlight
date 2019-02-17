package pl.sszwaczyk.forwarding;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.forwarding.Forwarding;
import pl.sszwaczyk.service.IServiceService;
import pl.sszwaczyk.service.IUserService;

import java.util.Collection;

public class SecurityForwarding extends Forwarding {

    private IUserService userService;
    private IServiceService serviceService;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> moduleDependencies = super.getModuleDependencies();
        moduleDependencies.add(IUserService.class);
        moduleDependencies.add(IServiceService.class);
        return moduleDependencies;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        super.init(context);
        userService = context.getServiceImpl(IUserService.class);
        serviceService = context.getServiceImpl(IServiceService.class);
        log.info("SecurityForwarding module initialized");
    }
}
