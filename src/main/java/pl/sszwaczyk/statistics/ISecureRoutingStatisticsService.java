package pl.sszwaczyk.statistics;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface ISecureRoutingStatisticsService extends IFloodlightService {

    SecureRoutingStatistics getSecureRoutingStatistics();

    /*
    Returns path to file
     */
    String snapshotStatisticsToFile(String statsFile);

}
