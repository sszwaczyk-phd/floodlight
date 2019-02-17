package pl.sszwaczyk.service;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.domain.Service;

public interface IServiceService extends IFloodlightService {

    Service getServiceById(String id);

}
