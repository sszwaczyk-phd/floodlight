package pl.sszwaczyk.uneven.calculator;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.util.Map;

public interface UnevenCalculator {

    UnevenMetric getMetric();

    Double calculateUneven(Map<NodePortTuple, SwitchPortBandwidth> bandwidth);

}
