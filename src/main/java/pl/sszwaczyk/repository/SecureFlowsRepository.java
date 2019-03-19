package pl.sszwaczyk.repository;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Path;
import org.projectfloodlight.openflow.protocol.OFFlowRemoved;
import org.projectfloodlight.openflow.protocol.OFFlowRemovedReason;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.TransportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.repository.web.SecureFlowsRepositoryRoutable;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.utils.AddressAndPort;
import pl.sszwaczyk.utils.AddressesAndPorts;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SecureFlowsRepository implements IFloodlightModule, ISecureFlowsRepository, IOFMessageListener {

    private Logger log = LoggerFactory.getLogger(SecureFlowsRepository.class);

    private IFloodlightProviderService floodlightProviderService;
    private IRestApiService restApiService;

    private List<Flow> finishedFlows = new ArrayList<>();

    private List<Flow> pendingFlows = new ArrayList<>();

    private volatile Map<AddressesAndPorts, Path> actualPaths = new ConcurrentHashMap<>();

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
        floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProviderService.addOFMessageListener(OFType.FLOW_REMOVED, this);
        restApiService.addRestletRoutable(new SecureFlowsRepositoryRoutable());
    }

    @Override
    public void registerFlow(Flow flow) {
        log.debug("Registering flow...");
        Decision decision = flow.getDecisions().get(0);
        if(!decision.isSolved()) {
            decision.setPath(null);
            flow.setFlowStatus(FlowStatus.NOT_REALIZED);
            flow.setEndTime(LocalTime.now());
            flow.setDuration(ChronoUnit.MILLIS.between(flow.getStartTime(), flow.getEndTime()));
            finishedFlows.add(flow);
            log.debug("Decision was to not solved so flow registered as not realized");
        } else {
            pendingFlows.add(flow);
            actualPaths.put(flow.getAp(), decision.getPath());
            log.debug("Flow {} added to pending flows", flow);
        }
    }

    @Override
    public List<Flow> getFinishedFlows() {
        return finishedFlows;
    }

    public Flow getPendingFlow(AddressesAndPorts addressesAndPorts) {
        return pendingFlows.stream().filter(flow -> flow.getAp().equals(addressesAndPorts)).findFirst().orElse(null);
    }

    @Override
    public List<Flow> getPendingFlows() {
        return pendingFlows;
    }

    @Override
    public void addDecision(AddressesAndPorts ap, Decision decision) {
        Flow pendingFlow = getPendingFlow(ap);
        List<Decision> decisions = pendingFlow.getDecisions();

        if(decisions == null) {
            decisions = new ArrayList<>();
        }
        decisions.add(decision);

        if(decision.isSolved()) {
            actualPaths.put(ap, decision.getPath());
        } else {
            decision.setPath(null);
            pendingFlow.setFlowStatus(FlowStatus.NOT_REALIZED);
            pendingFlow.setEndTime(LocalTime.now());
            pendingFlow.setDuration(ChronoUnit.MILLIS.between(pendingFlow.getStartTime(), pendingFlow.getEndTime()));
            finishedFlows.add(pendingFlow);
            pendingFlows.remove(pendingFlow);
        }
    }

    @Override
    public Map<AddressesAndPorts, Path> getActualPaths() {
        return actualPaths;
    }

    @Override
    public void deleteActualPath(AddressesAndPorts ap) {
        actualPaths.remove(ap);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch (msg.getType()) {
            case FLOW_REMOVED:
                log.debug("Flow removed message received");
                OFFlowRemoved flowRemoved = (OFFlowRemoved) msg;
                if(flowRemoved.getReason().equals(OFFlowRemovedReason.IDLE_TIMEOUT)) {
                    log.debug("Flow removed because of IDLE_TIMEOUT");
                    Match match = flowRemoved.getMatch();
                    EthType ethType = match.get(MatchField.ETH_TYPE);
                    if(ethType != null && ethType.equals(EthType.IPv4)) {
                        IPv4Address srcIpv4 = match.get(MatchField.IPV4_SRC);
                        IPv4Address dstIpv4 = match.get(MatchField.IPV4_DST);
                        IpProtocol ipProtocol = match.get(MatchField.IP_PROTO);
                        if(srcIpv4 != null && dstIpv4 != null && ipProtocol != null && ipProtocol.equals(IpProtocol.TCP)) {
                            TransportPort srcPort = match.get(MatchField.TCP_SRC);
                            TransportPort dstPort = match.get(MatchField.TCP_DST);
                            if(srcPort != null && dstPort != null) {
                                log.debug("Flow removed for src IP " + srcIpv4.toString() + ", dst IP " + dstIpv4.toString() + ", src TCP port " + srcPort.getPort() + " and dst TCP port " + dstPort.getPort());
                                AddressesAndPorts ap = AddressesAndPorts.builder()
                                        .src(AddressAndPort.builder()
                                                .address(srcIpv4.toString())
                                                .port(srcPort.getPort())
                                                .build())
                                        .dst(AddressAndPort.builder()
                                                .address(dstIpv4.toString())
                                                .port(dstPort.getPort())
                                                .build())
                                        .build();
                                Path remove = actualPaths.remove(ap);
                                if(remove != null) {
                                    log.info("Path " + remove + " for " + ap + " removed because of FLOW_REMOVED message");
                                    Flow pendingFlow = getPendingFlow(ap);
                                    pendingFlow.setFlowStatus(FlowStatus.FINISHED);
                                    pendingFlow.setEndTime(LocalTime.now());
                                    pendingFlow.setDuration(ChronoUnit.MILLIS.between(pendingFlow.getStartTime(), pendingFlow.getEndTime()));
                                    pendingFlows.remove(pendingFlow);
                                    finishedFlows.add(pendingFlow);
                                }
                            }
                        }
                    }
                }
            default:
                break;
        }
        return Command.CONTINUE;
    }

    @Override
    public String getName() {
        return "SecureFlowsRepository";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }
}
