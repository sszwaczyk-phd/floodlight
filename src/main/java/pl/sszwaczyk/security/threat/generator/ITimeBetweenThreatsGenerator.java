package pl.sszwaczyk.security.threat.generator;

public interface ITimeBetweenThreatsGenerator {

    /**
     * Generates time between threats in miliseconds
     * @return time between threats in miliseconds
     */
    long generateTimeBetweenThreats();

}
