package pl.sszwaczyk.repository.link;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.util.List;
import java.util.Map;

public interface ILinkStatisticsRepository extends IFloodlightService {

    List<MaxLinkUtilization> getMaxLinksBandwidth();

    Map<UnevenMetric, Double> getMaxUneven();
}
