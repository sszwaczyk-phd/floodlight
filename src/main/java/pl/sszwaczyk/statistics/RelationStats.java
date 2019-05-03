package pl.sszwaczyk.statistics;

import lombok.Data;
import pl.sszwaczyk.routing.solver.Reason;
import pl.sszwaczyk.routing.solver.SolveRegion;

@Data
public class RelationStats {

    int generated;

    int realized;

    int realizedInRarBf;
    float totalRiskInRarBf;
    float riskPerReqInRarBf;

    int realizedInRarRf;
    float totalRiskInRarRf;
    float riskPerReqInRarRf;

    int notRealized;
    int notRealizedBandwidth;
    int notRealizedDTSP;
    int notRealizedLatency;
    int notRealizedPending;

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
        } else if(reason.equals(Reason.CANNOT_FULFILL_DTSP)){
            notRealizedDTSP++;
        } else {
            notRealizedLatency++;
        }
    }

    public void updateNotRealizedPending() {
        generated++;
        notRealized++;
        notRealizedPending++;
    }
}
