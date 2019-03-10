package pl.sszwaczyk.statistics;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.statistics.web.SecureRoutingStatisticsRoutable;
import pl.sszwaczyk.user.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
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

        createDecisionsStatitisticsSheet(workbook);

//        createGeneralStatisticsSheet(workbook);
//
//        createRelationsStatisticsSheet(workbook);
//
//        createListStatitistcsSheet(workbook);

        try (FileOutputStream fos = new FileOutputStream(statsFile)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Cannot save secure routing statistics to file " + statsFile + " because " + e.getMessage());
        }

        log.info("Snapshot of secure routing statistics saved to " + statsFile);
        return statsFile;
    }

    private void createDecisionsStatitisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Decisions");

        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("ID");
        row0.createCell(1).setCellValue("User");
        row0.createCell(2).setCellValue("Service");
        row0.createCell(3).setCellValue("Acceptable Risk C");
        row0.createCell(4).setCellValue("Acceptable Risk I");
        row0.createCell(5).setCellValue("Acceptable Risk A");
        row0.createCell(6).setCellValue("Acceptable Risk T");
        row0.createCell(7).setCellValue("Max Risk C");
        row0.createCell(8).setCellValue("Max Risk I");
        row0.createCell(9).setCellValue("Max Risk A");
        row0.createCell(10).setCellValue("Max Risk T");
        row0.createCell(11).setCellValue("Date");
        row0.createCell(12).setCellValue("Time [ms]");
        row0.createCell(13).setCellValue("Solved");
        row0.createCell(14).setCellValue("Reason");
        row0.createCell(15).setCellValue("Uneven before");
        row0.createCell(16).setCellValue("Uneven after");
        row0.createCell(17).setCellValue("Region");
        row0.createCell(18).setCellValue("Value");
        row0.createCell(19).setCellValue("Risk C");
        row0.createCell(20).setCellValue("Risk I");
        row0.createCell(21).setCellValue("Risk A");
        row0.createCell(22).setCellValue("Risk T");
        row0.createCell(23).setCellValue("Aggregated Risk");
        row0.createCell(24).setCellValue("Path length");
        row0.createCell(25).setCellValue("Path");

        List<Decision> decisions = statistics.getDecisions();
        int i = 1;
        for(Decision decision: decisions) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(decision.getId());
            row.createCell(1).setCellValue(decision.getUser().getId());
            row.createCell(2).setCellValue(decision.getService().getId());

            Map<SecurityDimension, Float> acceptableRisks = decision.getAcceptableRisks();
            row.createCell(3).setCellValue(acceptableRisks.get(SecurityDimension.CONFIDENTIALITY));
            row.createCell(4).setCellValue(acceptableRisks.get(SecurityDimension.INTEGRITY));
            row.createCell(5).setCellValue(acceptableRisks.get(SecurityDimension.AVAILABILITY));
            row.createCell(6).setCellValue(acceptableRisks.get(SecurityDimension.TRUST));

            Map<SecurityDimension, Float> maxRisks = decision.getMaxRisks();
            row.createCell(7).setCellValue(maxRisks.get(SecurityDimension.CONFIDENTIALITY));
            row.createCell(8).setCellValue(maxRisks.get(SecurityDimension.INTEGRITY));
            row.createCell(9).setCellValue(maxRisks.get(SecurityDimension.AVAILABILITY));
            row.createCell(10).setCellValue(maxRisks.get(SecurityDimension.TRUST));

            row.createCell(11).setCellValue(decision.getDate());
            row.createCell(12).setCellValue(decision.getTime());

            boolean solved = decision.isSolved();
            row.createCell(13).setCellValue(solved);
            if(!solved) {
                row.createCell(14).setCellValue(decision.getReason().toString());
                continue;
            }

            row.createCell(15).setCellValue(decision.getUnevenBefore());
            row.createCell(16).setCellValue(decision.getUnevenAfter());
            row.createCell(17).setCellValue(decision.getRegion().toString());
            row.createCell(18).setCellValue(decision.getValue());
            Map<SecurityDimension, Float> risks = decision.getRisks();
            row.createCell(19).setCellValue(risks.get(SecurityDimension.CONFIDENTIALITY));
            row.createCell(20).setCellValue(risks.get(SecurityDimension.INTEGRITY));
            row.createCell(21).setCellValue(risks.get(SecurityDimension.AVAILABILITY));
            row.createCell(22).setCellValue(risks.get(SecurityDimension.TRUST));
            row.createCell(23).setCellValue(decision.getRisk());
            row.createCell(24).setCellValue(decision.getPathLength());
            row.createCell(25).setCellValue(decision.getPath().toString());
        }
    }

    private void createGeneralStatisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("General");

        sheet.createRow(0).createCell(0).setCellValue("Total");
        sheet.createRow(1).createCell(0).setCellValue("Realized");
        sheet.createRow(2).createCell(0).setCellValue("Realized RAR-BF");
        sheet.createRow(3).createCell(0).setCellValue("Realized RAR-RF");
        sheet.createRow(4).createCell(0).setCellValue("Not realized");

        sheet.getRow(0).createCell(1).setCellValue(statistics.getTotal());
        sheet.getRow(1).createCell(1).setCellValue(statistics.getRealized());
        sheet.getRow(2).createCell(1).setCellValue(statistics.getRealizedInRarBf());
        sheet.getRow(3).createCell(1).setCellValue(statistics.getRealizedInRarRf());
        sheet.getRow(4).createCell(1).setCellValue(statistics.getNotRealized());
    }

    private void createRelationsStatisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Relations");

        Row row0 = sheet.createRow(0);
        row0.createCell(2).setCellValue("Realized");
        row0.createCell(3).setCellValue("Realized RAR-BF");
        row0.createCell(4).setCellValue("Realized RAR-RF");
        row0.createCell(5).setCellValue("Not realized");

        int i = 1;
        Map<User, Map<Service, RelationStats>> relationStatsMap = statistics.getRelationStatsMap();
        for(User u: relationStatsMap.keySet()) {
            sheet.createRow(i).createCell(0).setCellValue(u.getId());
            Map<Service, RelationStats> serviceRelationStatsMap = relationStatsMap.get(u);
            for(Service s: serviceRelationStatsMap.keySet()) {
                Row row = sheet.getRow(i);
                if(row == null) {
                    row = sheet.createRow(i);
                }
                row.createCell(1).setCellValue(s.getId());
                RelationStats relationStats = serviceRelationStatsMap.get(s);
                row.createCell(2).setCellValue(relationStats.getRealized());
                row.createCell(3).setCellValue(relationStats.getRealizedInRarBf());
                row.createCell(4).setCellValue(relationStats.getRealizedInRarRf());
                row.createCell(5).setCellValue(relationStats.getNotRealized());
                i++;
            }
        }
    }

    private void createListStatitistcsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("List");

        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Time");
        row0.createCell(1).setCellValue("User");
        row0.createCell(2).setCellValue("Service");
        row0.createCell(3).setCellValue("Region");
        row0.createCell(4).setCellValue("Value");
        row0.createCell(5).setCellValue("Risk C");
        row0.createCell(6).setCellValue("Risk I");
        row0.createCell(7).setCellValue("Risk A");
        row0.createCell(8).setCellValue("Risk T");
        row0.createCell(9).setCellValue("Aggregated risk");
        row0.createCell(10).setCellValue("Path");

        List<ServerResponse> realizedList = statistics.getRealizedList();
        for(int i = 0; i < realizedList.size(); i++) {
            ServerResponse serverResponse = realizedList.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(serverResponse.getTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
            row.createCell(1).setCellValue(serverResponse.getUserId());
            row.createCell(2).setCellValue(serverResponse.getServiceId());
            row.createCell(3).setCellValue(serverResponse.getSolveResult().getRegion().toString());
            row.createCell(4).setCellValue(serverResponse.getSolveResult().getValue());
            Map<SecurityDimension, Float> risks = serverResponse.getSolveResult().getRisks();
            row.createCell(5).setCellValue(risks.get(SecurityDimension.CONFIDENTIALITY));
            row.createCell(6).setCellValue(risks.get(SecurityDimension.INTEGRITY));
            row.createCell(7).setCellValue(risks.get(SecurityDimension.AVAILABILITY));
            row.createCell(8).setCellValue(risks.get(SecurityDimension.TRUST));
            row.createCell(9).setCellValue(serverResponse.getSolveResult().getRisk());
            row.createCell(10).setCellValue(serverResponse.getSolveResult().getPath().toString());
        }
    }
}
