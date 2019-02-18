package pl.sszwaczyk.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DTSP {

    private Service service;
    private Map<SecurityDimension, Float> requirements;
    private Map<SecurityDimension, Float> consequences;
    private Map<SecurityDimension, Float> acceptableRiskIncrease;

}
