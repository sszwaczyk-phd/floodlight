package pl.sszwaczyk.uneven;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;

import java.util.Map;

public interface IUnevenService extends IFloodlightService {

    Map<UnevenMetric, Double> getUneven();

    Map<UnevenMetric, Double> getUneven(Map<NodePortTuple, SwitchPortBandwidth> bandwidthConsumption);

    Double getUneven(UnevenMetric metric);

    Double getUneven(UnevenMetric metric, Map<NodePortTuple, SwitchPortBandwidth> bandwidthConsumption);

}
