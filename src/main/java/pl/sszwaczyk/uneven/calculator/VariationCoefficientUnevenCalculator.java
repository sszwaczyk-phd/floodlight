package pl.sszwaczyk.uneven.calculator;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import pl.sszwaczyk.uneven.UnevenMetric;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariationCoefficientUnevenCalculator implements UnevenCalculator {

    @Override
    public UnevenMetric getMetric() {
        return UnevenMetric.VARIATION_COEFFICIENT;
    }

    @Override
    public Double calculateUneven(Map<NodePortTuple, SwitchPortBandwidth> bandwidth) {

        List<Double> txBands = bandwidth.values().stream()
                .filter(b -> !b.getSwitchPort().toString().equals("local"))
                .filter(b -> !b.getSwitchPort().toString().equals("controller"))
                .map(SwitchPortBandwidth::getTxUtilization)
                .collect(Collectors.toList());
        double[] array = new double[txBands.size()];
        for(int i = 0; i < txBands.size(); i++) array[i] = txBands.get(i);

        StandardDeviation standardDeviation = new StandardDeviation();
        double deviation = standardDeviation.evaluate(array);

        Mean mean = new Mean();
        double average = mean.evaluate(array);

        return deviation / average;
    }
}
