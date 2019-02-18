package pl.sszwaczyk.service;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface IServiceService extends IFloodlightService {

    Service getServiceById(String id);

}
