package pl.sszwaczyk.path;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Path;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

public interface IPathPropertiesService extends IFloodlightService {

    Map<SecurityDimension, Float> calculatePathProperties(Path path);

}
