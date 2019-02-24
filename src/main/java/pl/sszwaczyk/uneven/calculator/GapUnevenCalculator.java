package pl.sszwaczyk.uneven.calculator;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.util.Map;

public class GapUnevenCalculator implements UnevenCalculator {

    @Override
    public UnevenMetric getMetric() {
        return UnevenMetric.GAP;
    }

    @Override
    public Double calculateUneven(Map<NodePortTuple, SwitchPortBandwidth> bandwidth) {
        //TODO: implement
        return 0.5d;
    }

}
