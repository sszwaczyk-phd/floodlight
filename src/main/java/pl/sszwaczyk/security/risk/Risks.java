package pl.sszwaczyk.security.risk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Risks {

    private Map<SecurityDimension, Float> acceptableRisks;
    private Map<SecurityDimension, Float> maxRisks;

}
