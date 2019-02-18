package pl.sszwaczyk.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DTSPJson {

    private String serviceId;
    private Map<SecurityDimension, Float> requirements;
    private Map<SecurityDimension, Float> consequences;
    private Map<SecurityDimension, Float> acceptableRiskIncrease;

}
