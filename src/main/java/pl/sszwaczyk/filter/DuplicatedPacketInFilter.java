package pl.sszwaczyk.filter;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.ForwardingBase;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.service.IServiceService;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.utils.AddressesAndPorts;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DuplicatedPacketInFilter implements IFloodlightModule, IOFMessageListener {

    private static Logger log = LoggerFactory.getLogger(DuplicatedPacketInFilter.class);

    private static int BUFFERING_TIME = 60; //seconds

    private IFloodlightProviderService floodlightProviderService;
    private IServiceService serviceService;

    private Map<AddressesAndPorts, LocalTime> buffered = new ConcurrentHashMap<>();


    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IServiceService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
        serviceService = context.getServiceImpl(IServiceService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new FlushExpired(), 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch (msg.getType()) {
            case PACKET_IN:
                Service service = serviceService.getServiceFromCntx(cntx);
                if(service != null) {
                    AddressesAndPorts addressesAndPorts = AddressesAndPorts.fromCntx(cntx);
                    LocalTime time = buffered.get(addressesAndPorts);
                    if(time == null) {
                        time = LocalTime.now();
                        log.info("Buffering " + addressesAndPorts + "on time " + time.format(DateTimeFormatter.ISO_LOCAL_TIME));
                        buffered.put(addressesAndPorts, time);
                        return Command.CONTINUE;
                    } else {
                        return Command.STOP;
                    }
                }
                break;
            default:
                break;
        }
        return Command.CONTINUE;
    }

    @Override
    public String getName() {
        return "duplicated-filter";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return (type.equals(OFType.PACKET_IN)) && (name.equals("forwarding"));
    }

    class FlushExpired implements Runnable {

        @Override
        public void run() {
            log.info("Flushing expired...");
            buffered.values().removeIf(time -> LocalTime.now().isAfter(time.plusSeconds(BUFFERING_TIME)));
            log.debug("Actual buffered:");
            buffered.keySet().forEach(k -> log.debug(k.toString()));
        }

    }
}
