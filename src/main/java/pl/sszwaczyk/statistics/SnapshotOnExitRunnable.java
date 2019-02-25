package pl.sszwaczyk.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotOnExitRunnable implements Runnable {

    private Logger log = LoggerFactory.getLogger(SnapshotOnExitRunnable.class);

    private String statsFile;
    private ISecureRoutingStatisticsService statisticsService;

    public SnapshotOnExitRunnable(String statsFile,
                                  ISecureRoutingStatisticsService statisticsService) {
        this.statsFile = statsFile;
        this.statisticsService = statisticsService;
    }

    @Override
    public void run() {
        log.info("Saving secure routing statistics to file " + statsFile + " on exit...");
        statisticsService.snapshotStatisticsToFile(statsFile);
    }
}
