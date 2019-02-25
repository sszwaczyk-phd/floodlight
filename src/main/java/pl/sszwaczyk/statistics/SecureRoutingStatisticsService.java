package pl.sszwaczyk.statistics;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.statistics.web.SecureRoutingStatisticsRoutable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class SecureRoutingStatisticsService implements IFloodlightModule, ISecureRoutingStatisticsService {

    private Logger log = LoggerFactory.getLogger(ISecureRoutingStatisticsService.class);

    private IRestApiService restApiService;

    private SecureRoutingStatistics statistics;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(ISecureRoutingStatisticsService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(ISecureRoutingStatisticsService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IRestApiService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);

        statistics = new SecureRoutingStatistics();

        Map<String, String> configParameters = context.getConfigParams(this);
        boolean snapshotOnExit = Boolean.parseBoolean(configParameters.get("snapshot-on-exit"));
        if(snapshotOnExit) {
            log.info("Snapshot statistics on exit enabled");
            String snapshotFile = configParameters.get("stats-snapshot-file");
            if(snapshotFile == null || snapshotFile.isEmpty()) {
                snapshotFile = "/tmp/" + UUID.randomUUID() + ".xlsx";
                log.info("Snapshot file not specified. Statistics will be saved to " + snapshotFile);
            } else {
                log.info("Statistics will be saved to " + snapshotFile);
            }
            Thread thread = new Thread(new SnapshotOnExitRunnable(snapshotFile, this));
            Runtime.getRuntime().addShutdownHook(thread);
        }
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new SecureRoutingStatisticsRoutable());
    }


    @Override
    public SecureRoutingStatistics getSecureRoutingStatistics() {
        return statistics;
    }

    @Override
    public String snapshotStatisticsToFile(String statsFile) {
        log.info("Saving secure routing statistics to file...");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Statistics");

        sheet.createRow(0).createCell(0).setCellValue("Success");
        sheet.createRow(1).createCell(0).setCellValue("Failed");

        sheet.getRow(0).createCell(1).setCellValue(statistics.getRealizedRequests());
        sheet.getRow(1).createCell(1).setCellValue(statistics.getNotRealizedRequests());

        try (FileOutputStream fos = new FileOutputStream(statsFile)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Cannot save secure routing statistics to file " + statsFile + " because " + e.getMessage());
        }

        log.info("Snapshot of secure routing statistics saved to " + statsFile);
        return statsFile;
    }
}
