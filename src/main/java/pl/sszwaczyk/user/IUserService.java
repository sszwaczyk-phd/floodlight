package pl.sszwaczyk.user;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.module.IFloodlightService;

public interface IUserService extends IFloodlightService {

    User getUserByIp(String ip);

    User getUserFromCntx(FloodlightContext cntx);

}
