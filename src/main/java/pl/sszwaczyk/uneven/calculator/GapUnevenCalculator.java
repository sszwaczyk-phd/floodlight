package pl.sszwaczyk.uneven.calculator;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.util.Map;

public class GapUnevenCalculator implements UnevenCalculator {

    private Logger log = LoggerFactory.getLogger(GapUnevenCalculator.class);

    @Override
    public UnevenMetric getMetric() {
        return UnevenMetric.GAP;
    }

    @Override
    public Double calculateUneven(Map<NodePortTuple, SwitchPortBandwidth> bandwidth) {
        if(bandwidth.keySet().size() == 0) {
            return 0d;
        }

        double min = 1;
        double max = 0;

        for(SwitchPortBandwidth b: bandwidth.values()) {
            if(b.getSwitchPort().toString().equals("local")) {
                continue;
            }

            double txUtilization = b.getTxUtilization();
            log.debug("Utilization of switch " + b.getSwitchId() + " on port " + b.getSwitchPort().getPortNumber() + " is " + b.getTxUtilization());
            if(txUtilization < min) {
                min = txUtilization;
                log.debug("Min utilization set to " + min);
            }
            if(txUtilization > max) {
                max = txUtilization;
                log.debug("Max utilization set to " + max);
            }
        }

        return max - min;
    }

}
