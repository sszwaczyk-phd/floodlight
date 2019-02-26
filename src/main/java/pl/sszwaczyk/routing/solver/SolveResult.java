package pl.sszwaczyk.routing.solver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.floodlightcontroller.routing.Path;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolveResult {

    private boolean solved;

    private Path path;
    private SolveRegion region;
    private double value;

}
