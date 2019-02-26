package pl.sszwaczyk.statistics;

import lombok.Data;
import pl.sszwaczyk.routing.solver.SolveRegion;

@Data
public class RelationStats {

    int realized;
    int realizedInRarBf;
    int realizedInRarRf;

    int notRealized;

    //TODO: risks

    public void updateRealized(SolveRegion solveRegion) {
        realized++;
        if(solveRegion.equals(SolveRegion.RAR_BF)) {
            realizedInRarBf++;
        } else {
            realizedInRarRf++;
        }
    }

    public void updateNotRealized() {
        notRealized++;
    }
}
