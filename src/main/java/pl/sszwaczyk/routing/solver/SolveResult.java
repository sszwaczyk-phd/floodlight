package pl.sszwaczyk.routing.solver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.floodlightcontroller.routing.Path;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolveResult {

    private boolean solved;

    private Path path;
    private SolveRegion region;
    private double value;
    private Map<SecurityDimension, Float> risks;
    private float risk;

}
