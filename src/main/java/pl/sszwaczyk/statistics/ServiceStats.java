package pl.sszwaczyk.statistics;

import lombok.Data;
import pl.sszwaczyk.routing.solver.Reason;
import pl.sszwaczyk.routing.solver.SolveRegion;
import pl.sszwaczyk.service.Service;

@Data
public class ServiceStats {

    private Service service;

    public ServiceStats(Service service) {
        this.service = service;
    }

    private int generated;

    private int realized;

    private int realizedInRarBf;
    private float totalRiskInRarBf;
    private float riskPerReqInRarBf;

    private int realizedInRarRf;
    private float totalRiskInRarRf;
    private float riskPerReqInRarRf;

    private int notRealized;
    private int notRealizedDTSP;
    private float notRealizedBandwidth;

    public void updateRealized(SolveRegion solveRegion, float risk) {
        generated++;
        realized++;
        if(solveRegion.equals(SolveRegion.RAR_BF)) {
            realizedInRarBf++;
            totalRiskInRarBf += risk;
            riskPerReqInRarBf = totalRiskInRarBf / realizedInRarBf;
        } else {
            realizedInRarRf++;
            totalRiskInRarRf += risk;
            riskPerReqInRarRf = totalRiskInRarRf / realizedInRarRf;
        }
    }

    public void updateNotRealized(Reason reason) {
        generated++;
        notRealized++;
        if(reason.equals(Reason.CANNOT_FULFILL_BANDWIDTH)) {
            notRealizedBandwidth++;
        } else {
            notRealizedDTSP++;
        }
    }

}
