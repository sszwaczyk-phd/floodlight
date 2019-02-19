package pl.sszwaczyk.service;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.module.IFloodlightService;

public interface IServiceService extends IFloodlightService {

    Service getServiceById(String id);

    Service getServiceByAddrAndPort(String srcAddr, int srcPort);

    Service getServiceFromCntx(FloodlightContext cntx);

}
