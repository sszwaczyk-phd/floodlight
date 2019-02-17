package pl.sszwaczyk.service;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.domain.User;

public interface IUserService extends IFloodlightService {

    User getUserByIp(String ip);

}
