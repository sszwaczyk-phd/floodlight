package pl.sszwaczyk.statistics;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.repository.flow.Flow;
import pl.sszwaczyk.repository.flow.FlowStatus;
import pl.sszwaczyk.repository.flow.ISecureFlowsRepository;
import pl.sszwaczyk.repository.link.ILinkStatisticsRepository;
import pl.sszwaczyk.repository.link.MaxLinkUtilization;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.routing.solver.Reason;
import pl.sszwaczyk.routing.solver.SolveRegion;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.security.threat.ThreatWithInfluence;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.statistics.web.SecureRoutingStatisticsRoutable;
import pl.sszwaczyk.uneven.UnevenMetric;
import pl.sszwaczyk.user.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SecureRoutingStatisticsService implements IFloodlightModule, ISecureRoutingStatisticsService {

    private Logger log = LoggerFactory.getLogger(ISecureRoutingStatisticsService.class);

    private ISecureFlowsRepository secureFlowsRepository;
    private ILinkStatisticsRepository linkStatisticsRepository;
    private IRestApiService restApiService;

    private SecureRoutingStatistics statistics;

    private static PendingClassification PENDING_CLASSIFICATION = PendingClassification.REALIZED;

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
        l.add(ISecureFlowsRepository.class);
        l.add(ILinkStatisticsRepository.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        secureFlowsRepository = context.getServiceImpl(ISecureFlowsRepository.class);
        linkStatisticsRepository = context.getServiceImpl(ILinkStatisticsRepository.class);
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

        PENDING_CLASSIFICATION = PendingClassification.valueOf(configParameters.get("pending-classification"));
        log.info("Pending classification set to " + PENDING_CLASSIFICATION);
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

        createThreatsStatisticsSheet(workbook);

        createFinishedFlowsSheet(workbook);

        createPendingFlowsSheet(workbook);

        createLinksStatisticsSheet(workbook);

        createUnevenStatisticsSheet(workbook);

        createGeneralAndUsersAndServicesStatisticsSheet(workbook);

        try (FileOutputStream fos = new FileOutputStream(statsFile)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Cannot save secure routing statistics to file " + statsFile + " because " + e.getMessage());
        }

        log.info("Snapshot of secure routing statistics saved to " + statsFile);
        return statsFile;
    }

    private void createGeneralAndUsersAndServicesStatisticsSheet(Workbook workbook) {
        AtomicLong total = new AtomicLong();
        AtomicLong realized = new AtomicLong();
        AtomicLong realizedRarBf = new AtomicLong();
        float totalRiskRarBf = 0;
        float riskPerReqRarBf = 0;
        AtomicLong realizedRarRf = new AtomicLong();
        float totalRiskRarRf = 0;
        float riskPerReqRarRf = 0;
        AtomicLong notRealized = new AtomicLong();
        AtomicLong notRealizedBandwidth = new AtomicLong();
        AtomicLong notRealizedDTSP = new AtomicLong();
        AtomicLong notRealizedLatency = new AtomicLong();

        Map<User, Map<Service, RelationStats>> relationStatsMap = new HashMap<>();
        List<ServiceStats> serviceStatsList = new ArrayList<>();

        for (Flow flow : secureFlowsRepository.getFinishedFlows()) {
            User user = flow.getUser();
            Service service = flow.getService();
            Map<Service, RelationStats> serviceRelationStatsMap = relationStatsMap.get(user);
            RelationStats relationStats;
            if (serviceRelationStatsMap == null) {
                serviceRelationStatsMap = new HashMap<>();
                relationStats = new RelationStats();
            } else {
                relationStats = serviceRelationStatsMap.get(service);
                if (relationStats == null) {
                    relationStats = new RelationStats();
                }
            }
            serviceRelationStatsMap.put(service, relationStats);
            relationStatsMap.put(user, serviceRelationStatsMap);

            ServiceStats serviceStats;
            Optional<ServiceStats> optional = serviceStatsList.stream().filter(ss -> ss.getService().getId().equals(service.getId()))
                    .findFirst();
            if(optional.isPresent()) {
                serviceStats = optional.get();
            } else {
                serviceStats = new ServiceStats(service);
                serviceStatsList.add(serviceStats);
            }

            total.getAndIncrement();
            Decision lastDecision = flow.getDecisions().get(flow.getDecisions().size() - 1);
            if (flow.getFlowStatus().equals(FlowStatus.FINISHED)) {
                realized.getAndIncrement();
                float risk = lastDecision.getRisk();
                if (lastDecision.getRegion().equals(SolveRegion.RAR_BF)) {
                    realizedRarBf.getAndIncrement();
                    totalRiskRarBf += risk;
                    riskPerReqRarBf = totalRiskRarBf / realizedRarBf.get();
                } else {
                    realizedRarRf.getAndIncrement();
                    totalRiskRarRf += risk;
                    riskPerReqRarRf = totalRiskRarRf / realizedRarRf.get();
                }
                relationStats.updateRealized(lastDecision.getRegion(), risk);
                serviceStats.updateRealized(lastDecision.getRegion(), risk);
            } else if (flow.getFlowStatus().equals(FlowStatus.NOT_REALIZED)) {
                notRealized.getAndIncrement();
                Reason reason = lastDecision.getReason();
                relationStats.updateNotRealized(reason);
                serviceStats.updateNotRealized(reason);
                if(reason.equals(Reason.CANNOT_FULFILL_BANDWIDTH)) {
                    notRealizedBandwidth.getAndIncrement();
                } else if(reason.equals(Reason.CANNOT_FULFILL_DTSP)) {
                    notRealizedDTSP.getAndIncrement();
                } else {
                    notRealizedLatency.getAndIncrement();
                }
            }
        }

        AtomicLong notRealizedPending = new AtomicLong();
        if(PENDING_CLASSIFICATION.equals(PendingClassification.REALIZED)) {

            for (Flow flow : secureFlowsRepository.getPendingFlows()) {
                User user = flow.getUser();
                Service service = flow.getService();
                Map<Service, RelationStats> serviceRelationStatsMap = relationStatsMap.get(user);
                RelationStats relationStats;
                if (serviceRelationStatsMap == null) {
                    serviceRelationStatsMap = new HashMap<>();
                    relationStats = new RelationStats();
                } else {
                    relationStats = serviceRelationStatsMap.get(service);
                    if (relationStats == null) {
                        relationStats = new RelationStats();
                    }
                }
                serviceRelationStatsMap.put(service, relationStats);
                relationStatsMap.put(user, serviceRelationStatsMap);

                ServiceStats serviceStats;
                Optional<ServiceStats> optional = serviceStatsList.stream().filter(ss -> ss.getService().getId().equals(service.getId()))
                        .findFirst();
                if (optional.isPresent()) {
                    serviceStats = optional.get();
                } else {
                    serviceStats = new ServiceStats(service);
                    serviceStatsList.add(serviceStats);
                }

                total.getAndIncrement();
                Decision lastDecision = flow.getDecisions().get(flow.getDecisions().size() - 1);
                realized.getAndIncrement();
                float risk = lastDecision.getRisk();
                if (lastDecision.getRegion().equals(SolveRegion.RAR_BF)) {
                    realizedRarBf.getAndIncrement();
                    totalRiskRarBf += risk;
                    riskPerReqRarBf = totalRiskRarBf / realizedRarBf.get();
                } else {
                    realizedRarRf.getAndIncrement();
                    totalRiskRarRf += risk;
                    riskPerReqRarRf = totalRiskRarRf / realizedRarRf.get();
                }
                relationStats.updateRealized(lastDecision.getRegion(), risk);
                serviceStats.updateRealized(lastDecision.getRegion(), risk);
            }

        } else if(PENDING_CLASSIFICATION.equals(PendingClassification.NOT_REALIZED)) {

            for(Flow flow: secureFlowsRepository.getPendingFlows()) {
                User user = flow.getUser();
                Service service = flow.getService();
                Map<Service, RelationStats> serviceRelationStatsMap = relationStatsMap.get(user);
                RelationStats relationStats;
                if (serviceRelationStatsMap == null) {
                    serviceRelationStatsMap = new HashMap<>();
                    relationStats = new RelationStats();
                } else {
                    relationStats = serviceRelationStatsMap.get(service);
                    if (relationStats == null) {
                        relationStats = new RelationStats();
                    }
                }
                serviceRelationStatsMap.put(service, relationStats);
                relationStatsMap.put(user, serviceRelationStatsMap);

                ServiceStats serviceStats;
                Optional<ServiceStats> optional = serviceStatsList.stream().filter(ss -> ss.getService().getId().equals(service.getId()))
                        .findFirst();
                if (optional.isPresent()) {
                    serviceStats = optional.get();
                } else {
                    serviceStats = new ServiceStats(service);
                    serviceStatsList.add(serviceStats);
                }

                total.getAndIncrement();
                notRealized.getAndIncrement();
                relationStats.updateNotRealizedPending();
                serviceStats.updateNotRealizedPending();
                notRealizedPending.getAndIncrement();
            }

        }

        //general
        Sheet sheet = workbook.createSheet("General");

        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Total");
        row0.createCell(1).setCellValue("Realized");
        row0.createCell(2).setCellValue("Realized RAR-BF");
        row0.createCell(3).setCellValue("Total Risk RAR-BF");
        row0.createCell(4).setCellValue("Risk per req RAR-BF");
        row0.createCell(5).setCellValue("Realized RAR-RF");
        row0.createCell(6).setCellValue("Total risk RAR-RF");
        row0.createCell(7).setCellValue("Risk per req RAR-RF");
        row0.createCell(8).setCellValue("Not realized");
        row0.createCell(9).setCellValue("Not realized - BANDWIDTH");
        row0.createCell(10).setCellValue("Not realized - DTSP");
        row0.createCell(11).setCellValue("Not realized - LATENCY");
        row0.createCell(12).setCellValue("Not realized - PENDING");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue(total.get());
        row1.createCell(1).setCellValue(realized.get());
        row1.createCell(2).setCellValue(realizedRarBf.get());
        row1.createCell(3).setCellValue(totalRiskRarBf);
        row1.createCell(4).setCellValue(riskPerReqRarBf);
        row1.createCell(5).setCellValue(realizedRarRf.get());
        row1.createCell(6).setCellValue(totalRiskRarRf);
        row1.createCell(7).setCellValue(riskPerReqRarRf);
        row1.createCell(8).setCellValue(notRealized.get());
        row1.createCell(9).setCellValue(notRealizedBandwidth.get());
        row1.createCell(10).setCellValue(notRealizedDTSP.get());
        row1.createCell(11).setCellValue(notRealizedLatency.get());
        row1.createCell(12).setCellValue(notRealizedPending.get());

        //users
        sheet = workbook.createSheet("Users");

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 6));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 9));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 12));
        row0 = sheet.createRow(0);
        row0.createCell(4).setCellValue("RAR-BF");
        row0.createCell(7).setCellValue("RAR-RF");
        row0.createCell(10).setCellValue("Not realized");

        row1 = sheet.createRow(1);
        row1.createCell(2).setCellValue("Generated");
        row1.createCell(3).setCellValue("Total Realized");
        row1.createCell(4).setCellValue("Realized");
        row1.createCell(5).setCellValue("Total Risk");
        row1.createCell(6).setCellValue("Risk per req");
        row1.createCell(7).setCellValue("Realized");
        row1.createCell(8).setCellValue("Total Risk");
        row1.createCell(9).setCellValue("Risk per req");
        row1.createCell(10).setCellValue("Total");
        row1.createCell(11).setCellValue("DTSP");
        row1.createCell(12).setCellValue("BANDWIDTH");
        row1.createCell(13).setCellValue("LATENCY");
        row1.createCell(14).setCellValue("PENDING");

        int i = 2;

        for(User u: relationStatsMap.keySet()) {
            log.debug("Saving stats for user " + u.getId());
            sheet.createRow(i).createCell(0).setCellValue(u.getId());
            Map<Service, RelationStats> serviceRelationStatsMap = relationStatsMap.get(u);
            for(Service s: serviceRelationStatsMap.keySet()) {
                log.debug("Saving stats for service " + s.getId());
                Row row = sheet.getRow(i);
                if(row == null) {
                    row = sheet.createRow(i);
                }
                row.createCell(1).setCellValue(s.getId());
                RelationStats relationStats = serviceRelationStatsMap.get(s);
                row.createCell(2).setCellValue(relationStats.getGenerated());
                row.createCell(3).setCellValue(relationStats.getRealized());
                row.createCell(4).setCellValue(relationStats.getRealizedInRarBf());
                row.createCell(5).setCellValue(relationStats.getTotalRiskInRarBf());
                row.createCell(6).setCellValue(relationStats.getRiskPerReqInRarBf());
                row.createCell(7).setCellValue(relationStats.getRealizedInRarRf());
                row.createCell(8).setCellValue(relationStats.getTotalRiskInRarRf());
                row.createCell(9).setCellValue(relationStats.getRiskPerReqInRarRf());
                row.createCell(10).setCellValue(relationStats.getNotRealized());
                row.createCell(11).setCellValue(relationStats.getNotRealizedDTSP());
                row.createCell(12).setCellValue(relationStats.getNotRealizedBandwidth());
                row.createCell(13).setCellValue(relationStats.getNotRealizedLatency());
                row.createCell(14).setCellValue(relationStats.getNotRealizedPending());
                i++;
            }
        }

        //services
        sheet = workbook.createSheet("Services");

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 5));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 8));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 11));
        row0 = sheet.createRow(0);
        row0.createCell(3).setCellValue("RAR-BF");
        row0.createCell(6).setCellValue("RAR-RF");
        row0.createCell(9).setCellValue("Not realized");

        row1 = sheet.createRow(1);
        row1.createCell(1).setCellValue("Generated");
        row1.createCell(2).setCellValue("Total realized");
        row1.createCell(3).setCellValue("Realized");
        row1.createCell(4).setCellValue("Toal risk");
        row1.createCell(5).setCellValue("Risk per req");
        row1.createCell(6).setCellValue("Realized");
        row1.createCell(7).setCellValue("Toal risk");
        row1.createCell(8).setCellValue("Risk per req");
        row1.createCell(9).setCellValue("Total");
        row1.createCell(10).setCellValue("DTSP");
        row1.createCell(11).setCellValue("BANDWIDTH");
        row1.createCell(12).setCellValue("LATENCY");
        row1.createCell(13).setCellValue("PENDING");

        i = 2;
        for(ServiceStats serviceStats: serviceStatsList) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(serviceStats.getService().getId());
            row.createCell(1).setCellValue(serviceStats.getGenerated());
            row.createCell(2).setCellValue(serviceStats.getRealized());
            row.createCell(3).setCellValue(serviceStats.getRealizedInRarBf());
            row.createCell(4).setCellValue(serviceStats.getTotalRiskInRarBf());
            row.createCell(5).setCellValue(serviceStats.getRiskPerReqInRarBf());
            row.createCell(6).setCellValue(serviceStats.getRealizedInRarRf());
            row.createCell(7).setCellValue(serviceStats.getTotalRiskInRarRf());
            row.createCell(8).setCellValue(serviceStats.getRiskPerReqInRarRf());
            row.createCell(9).setCellValue(serviceStats.getNotRealized());
            row.createCell(10).setCellValue(serviceStats.getNotRealizedDTSP());
            row.createCell(11).setCellValue(serviceStats.getNotRealizedBandwidth());
            row.createCell(12).setCellValue(serviceStats.getNotRealizedLatency());
            row.createCell(13).setCellValue(serviceStats.getNotRealizedPending());
            i++;
        }
    }

    private void createPendingFlowsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Pending Flows");

        Row row0 = sheet.createRow(0);

        //FLOW
        row0.createCell(0).setCellValue("Flow start time");
        row0.createCell(1).setCellValue("Flow end time");
        row0.createCell(2).setCellValue("Flow duration [ms]");
        row0.createCell(3).setCellValue("Service");
        row0.createCell(4).setCellValue("Service Addr");
        row0.createCell(5).setCellValue("Service Port");
        row0.createCell(6).setCellValue("User");
        row0.createCell(7).setCellValue("User Addr");
        row0.createCell(8).setCellValue("User Port");
        row0.createCell(9).setCellValue("Status");

        //DECISION
        row0.createCell(10).setCellValue("ID");
        row0.createCell(11).setCellValue("User");
        row0.createCell(12).setCellValue("Service");
        row0.createCell(13).setCellValue("Acceptable Risk C");
        row0.createCell(14).setCellValue("Acceptable Risk I");
        row0.createCell(15).setCellValue("Acceptable Risk A");
        row0.createCell(16).setCellValue("Acceptable Risk T");
        row0.createCell(17).setCellValue("Max Risk C");
        row0.createCell(18).setCellValue("Max Risk I");
        row0.createCell(19).setCellValue("Max Risk A");
        row0.createCell(20).setCellValue("Max Risk T");
        row0.createCell(21).setCellValue("Date");
        row0.createCell(22).setCellValue("Time [ms]");
        row0.createCell(23).setCellValue("Solved");
        row0.createCell(24).setCellValue("Reason");
        row0.createCell(25).setCellValue("Uneven before");
        row0.createCell(26).setCellValue("Uneven after");
        row0.createCell(27).setCellValue("Region");
        row0.createCell(28).setCellValue("Value");
        row0.createCell(29).setCellValue("Risk C");
        row0.createCell(30).setCellValue("Risk I");
        row0.createCell(31).setCellValue("Risk A");
        row0.createCell(32).setCellValue("Risk T");
        row0.createCell(33).setCellValue("Aggregated Risk");
        row0.createCell(34).setCellValue("Path length");
        row0.createCell(35).setCellValue("Path");

        List<Flow> pendingFlows = secureFlowsRepository.getPendingFlows();
        int i = 1;
        for(Flow flow: pendingFlows) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(flow.getStartTime().toString());
//            row.createCell(1).setCellValue(flow.getEndTime().toString());
//            row.createCell(2).setCellValue(flow.getDuration());
            row.createCell(3).setCellValue(flow.getService().getId());
            row.createCell(4).setCellValue(flow.getAp().getSrc().getAddress());
            row.createCell(5).setCellValue(flow.getAp().getSrc().getPort());
            row.createCell(6).setCellValue(flow.getUser().getId());
            row.createCell(7).setCellValue(flow.getAp().getDst().getAddress());
            row.createCell(8).setCellValue(flow.getAp().getDst().getPort());
            row.createCell(9).setCellValue(flow.getFlowStatus().toString());

            List<Decision> decisions = flow.getDecisions();
            for(Decision decision: decisions) {
                Row decisionRow = sheet.createRow(i + 1);
                decisionRow.createCell(10).setCellValue(decision.getId());
                decisionRow.createCell(11).setCellValue(decision.getUser().getId());
                decisionRow.createCell(12).setCellValue(decision.getService().getId());

                Map<SecurityDimension, Float> acceptableRisks = decision.getAcceptableRisks();
                decisionRow.createCell(13).setCellValue(acceptableRisks.get(SecurityDimension.CONFIDENTIALITY));
                decisionRow.createCell(14).setCellValue(acceptableRisks.get(SecurityDimension.INTEGRITY));
                decisionRow.createCell(15).setCellValue(acceptableRisks.get(SecurityDimension.AVAILABILITY));
                decisionRow.createCell(16).setCellValue(acceptableRisks.get(SecurityDimension.TRUST));

                Map<SecurityDimension, Float> maxRisks = decision.getMaxRisks();
                decisionRow.createCell(17).setCellValue(maxRisks.get(SecurityDimension.CONFIDENTIALITY));
                decisionRow.createCell(18).setCellValue(maxRisks.get(SecurityDimension.INTEGRITY));
                decisionRow.createCell(19).setCellValue(maxRisks.get(SecurityDimension.AVAILABILITY));
                decisionRow.createCell(20).setCellValue(maxRisks.get(SecurityDimension.TRUST));

                decisionRow.createCell(21).setCellValue(decision.getDate().toString());
                decisionRow.createCell(22).setCellValue(decision.getTime());

                boolean solved = decision.isSolved();
                decisionRow.createCell(23).setCellValue(solved);
                if(!solved) {
                    decisionRow.createCell(24).setCellValue(decision.getReason().toString());
                    i++;
                    continue;
                }

                decisionRow.createCell(25).setCellValue(decision.getUnevenBefore());
                decisionRow.createCell(26).setCellValue(decision.getUnevenAfter());
                decisionRow.createCell(27).setCellValue(decision.getRegion().toString());
                decisionRow.createCell(28).setCellValue(decision.getValue());
                Map<SecurityDimension, Float> risks = decision.getRisks();
                decisionRow.createCell(29).setCellValue(risks.get(SecurityDimension.CONFIDENTIALITY));
                decisionRow.createCell(30).setCellValue(risks.get(SecurityDimension.INTEGRITY));
                decisionRow.createCell(31).setCellValue(risks.get(SecurityDimension.AVAILABILITY));
                decisionRow.createCell(32).setCellValue(risks.get(SecurityDimension.TRUST));
                decisionRow.createCell(33).setCellValue(decision.getRisk());
                decisionRow.createCell(34).setCellValue(decision.getPathLength());
                decisionRow.createCell(35).setCellValue(decision.getPath().toString());
                i++;
            }
            i++;
        }
    }

    private void createFinishedFlowsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Finished Flows");

        Row row0 = sheet.createRow(0);

        //FLOW
        row0.createCell(0).setCellValue("Flow start time");
        row0.createCell(1).setCellValue("Flow end time");
        row0.createCell(2).setCellValue("Flow duration [ms]");
        row0.createCell(3).setCellValue("Service");
        row0.createCell(4).setCellValue("Service Addr");
        row0.createCell(5).setCellValue("Service Port");
        row0.createCell(6).setCellValue("User");
        row0.createCell(7).setCellValue("User Addr");
        row0.createCell(8).setCellValue("User Port");
        row0.createCell(9).setCellValue("Status");

        //DECISION
        row0.createCell(10).setCellValue("ID");
        row0.createCell(11).setCellValue("User");
        row0.createCell(12).setCellValue("Service");
        row0.createCell(13).setCellValue("Acceptable Risk C");
        row0.createCell(14).setCellValue("Acceptable Risk I");
        row0.createCell(15).setCellValue("Acceptable Risk A");
        row0.createCell(16).setCellValue("Acceptable Risk T");
        row0.createCell(17).setCellValue("Max Risk C");
        row0.createCell(18).setCellValue("Max Risk I");
        row0.createCell(19).setCellValue("Max Risk A");
        row0.createCell(20).setCellValue("Max Risk T");
        row0.createCell(21).setCellValue("Date");
        row0.createCell(22).setCellValue("Time [ms]");
        row0.createCell(23).setCellValue("Solved");
        row0.createCell(24).setCellValue("Reason");
        row0.createCell(25).setCellValue("Uneven before");
        row0.createCell(26).setCellValue("Uneven after");
        row0.createCell(27).setCellValue("Region");
        row0.createCell(28).setCellValue("Value");
        row0.createCell(29).setCellValue("Risk C");
        row0.createCell(30).setCellValue("Risk I");
        row0.createCell(31).setCellValue("Risk A");
        row0.createCell(32).setCellValue("Risk T");
        row0.createCell(33).setCellValue("Aggregated Risk");
        row0.createCell(34).setCellValue("Path length");
        row0.createCell(35).setCellValue("Path");

        List<Flow> finishedFlows = secureFlowsRepository.getFinishedFlows();
        int i = 1;
        for(Flow flow: finishedFlows) {
            if(flow == null) {
                continue;
            }
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(flow.getStartTime().toString());
            row.createCell(1).setCellValue(flow.getEndTime().toString());
            row.createCell(2).setCellValue(flow.getDuration());
            row.createCell(3).setCellValue(flow.getService().getId());
            row.createCell(4).setCellValue(flow.getAp().getSrc().getAddress());
            row.createCell(5).setCellValue(flow.getAp().getSrc().getPort());
            row.createCell(6).setCellValue(flow.getUser().getId());
            row.createCell(7).setCellValue(flow.getAp().getDst().getAddress());
            row.createCell(8).setCellValue(flow.getAp().getDst().getPort());
            row.createCell(9).setCellValue(flow.getFlowStatus().toString());

            List<Decision> decisions = flow.getDecisions();
            for(Decision decision: decisions) {
                Row decisionRow = sheet.createRow(i + 1);
                decisionRow.createCell(10).setCellValue(decision.getId());
                decisionRow.createCell(11).setCellValue(decision.getUser().getId());
                decisionRow.createCell(12).setCellValue(decision.getService().getId());

                Map<SecurityDimension, Float> acceptableRisks = decision.getAcceptableRisks();
                decisionRow.createCell(13).setCellValue(acceptableRisks.get(SecurityDimension.CONFIDENTIALITY));
                decisionRow.createCell(14).setCellValue(acceptableRisks.get(SecurityDimension.INTEGRITY));
                decisionRow.createCell(15).setCellValue(acceptableRisks.get(SecurityDimension.AVAILABILITY));
                decisionRow.createCell(16).setCellValue(acceptableRisks.get(SecurityDimension.TRUST));

                Map<SecurityDimension, Float> maxRisks = decision.getMaxRisks();
                decisionRow.createCell(17).setCellValue(maxRisks.get(SecurityDimension.CONFIDENTIALITY));
                decisionRow.createCell(18).setCellValue(maxRisks.get(SecurityDimension.INTEGRITY));
                decisionRow.createCell(19).setCellValue(maxRisks.get(SecurityDimension.AVAILABILITY));
                decisionRow.createCell(20).setCellValue(maxRisks.get(SecurityDimension.TRUST));

                decisionRow.createCell(21).setCellValue(decision.getDate().toString());
                decisionRow.createCell(22).setCellValue(decision.getTime());

                boolean solved = decision.isSolved();
                decisionRow.createCell(23).setCellValue(solved);
                if(!solved) {
                    decisionRow.createCell(24).setCellValue(decision.getReason().toString());
                    i++;
                    continue;
                }

                decisionRow.createCell(25).setCellValue(decision.getUnevenBefore());
                decisionRow.createCell(26).setCellValue(decision.getUnevenAfter());
                decisionRow.createCell(27).setCellValue(decision.getRegion().toString());
                decisionRow.createCell(28).setCellValue(decision.getValue());
                Map<SecurityDimension, Float> risks = decision.getRisks();
                decisionRow.createCell(29).setCellValue(risks.get(SecurityDimension.CONFIDENTIALITY));
                decisionRow.createCell(30).setCellValue(risks.get(SecurityDimension.INTEGRITY));
                decisionRow.createCell(31).setCellValue(risks.get(SecurityDimension.AVAILABILITY));
                decisionRow.createCell(32).setCellValue(risks.get(SecurityDimension.TRUST));
                decisionRow.createCell(33).setCellValue(decision.getRisk());
                decisionRow.createCell(34).setCellValue(decision.getPathLength());
                decisionRow.createCell(35).setCellValue(decision.getPath().toString());
                i++;
            }
            i++;
        }

    }

    private void createThreatsStatisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Threats");

        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("ID");
        row0.createCell(1).setCellValue("Start time");
        row0.createCell(2).setCellValue("Duration");
        row0.createCell(3).setCellValue("Switches");
        row0.createCell(4).setCellValue("C");
        row0.createCell(5).setCellValue("I");
        row0.createCell(6).setCellValue("A");
        row0.createCell(7).setCellValue("T");


        int i = 1;
        for(ThreatWithInfluence twi: statistics.getThreats()) {
            Row row = sheet.createRow(i);

            row.createCell(0).setCellValue(twi.getThreat().getId());
            row.createCell(1).setCellValue(twi.getThreat().getStartTime().toString());
            row.createCell(2).setCellValue(twi.getThreat().getDuration());
            StringBuilder sb = new StringBuilder();
            for(DatapathId dpid: twi.getThreat().getSwitches()) {
                sb.append(dpid + " ; ");
            }
            row.createCell(3).setCellValue(sb.toString());

            row.createCell(4).setCellValue(twi.getInfluence().get(SecurityDimension.CONFIDENTIALITY));
            row.createCell(5).setCellValue(twi.getInfluence().get(SecurityDimension.INTEGRITY));
            row.createCell(6).setCellValue(twi.getInfluence().get(SecurityDimension.AVAILABILITY));
            row.createCell(7).setCellValue(twi.getInfluence().get(SecurityDimension.TRUST));
            i++;
        }
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
            if(decision == null) {
                continue;
            }
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

            row.createCell(11).setCellValue(decision.getDate().toString());
            row.createCell(12).setCellValue(decision.getTime());

            boolean solved = decision.isSolved();
            row.createCell(13).setCellValue(solved);
            if(!solved) {
                row.createCell(14).setCellValue(decision.getReason().toString());
                i++;
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
            i++;
        }
    }

    private void createLinksStatisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Links Stats");

        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Datapath ID");
        row0.createCell(1).setCellValue("Port");
        row0.createCell(2).setCellValue("Max TX Utilization");
        row0.createCell(3).setCellValue("Max TX Utilization [%]");
        row0.createCell(4).setCellValue("Max RX Utilization");
        row0.createCell(5).setCellValue("Max RX Utilization [%]");

        int i = 1;
        List<MaxLinkUtilization> maxLinksBandwidth = linkStatisticsRepository.getMaxLinksBandwidth();
        for(MaxLinkUtilization max: maxLinksBandwidth) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(max.getId().toString());
            row.createCell(1).setCellValue(max.getPt().getPortNumber());
            row.createCell(2).setCellValue(max.getMaxTxUtilization());
            row.createCell(3).setCellValue(max.getMaxTxUtilizationPercent());
            row.createCell(4).setCellValue(max.getMaxRxUtilization());
            row.createCell(5).setCellValue(max.getMaxRxUtilizationPercent());
            i++;
        }

    }

    private void createUnevenStatisticsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Uneven stats");

        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Metric");
        row0.createCell(1).setCellValue("Max value");

        int i = 1;
        Map<UnevenMetric, Double> maxUneven = linkStatisticsRepository.getMaxUneven();
        for(Map.Entry<UnevenMetric, Double> entry: maxUneven.entrySet()) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(entry.getKey().toString());
            row.createCell(1).setCellValue(entry.getValue());
            i++;
        }
    }
}
