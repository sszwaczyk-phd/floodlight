package pl.sszwaczyk.forwarding;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.forwarding.Forwarding;
import net.floodlightcontroller.routing.IRoutingDecision;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
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
        log.info("Security Forwarding module initialized");
    }

    @Override
    public Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, IRoutingDecision decision, FloodlightContext cntx) {
        return super.processPacketInMessage(sw, pi, decision, cntx);
    }
}
