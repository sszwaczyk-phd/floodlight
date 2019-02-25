package pl.sszwaczyk.uneven;

import net.floodlightcontroller.core.module.IFloodlightService;

import java.util.Map;

public interface IUnevenService extends IFloodlightService {

    Map<UnevenMetric, Double> getUneven();

    Double getUneven(UnevenMetric metric);

}
