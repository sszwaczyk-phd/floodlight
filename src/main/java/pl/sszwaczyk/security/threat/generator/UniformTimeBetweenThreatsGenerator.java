package pl.sszwaczyk.security.threat.generator;

import lombok.AllArgsConstructor;

import java.util.Random;

public class UniformTimeBetweenThreatsGenerator implements ITimeBetweenThreatsGenerator {

    private final Random random;
    private final int minGap;
    private final int maxGap;

    public UniformTimeBetweenThreatsGenerator(int randomSeed, int minGap, int maxGap) {
        this.random = new Random(randomSeed);
        this.minGap = minGap;
        this.maxGap = maxGap;
    }

    @Override
    public long generateTimeBetweenThreats() {
        int gap = random.nextInt((maxGap - minGap) + 1) + minGap;
        return gap * 1000;
    }

}
