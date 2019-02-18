package pl.sszwaczyk.domain.security.risk;

import pl.sszwaczyk.domain.SecurityDimension;

import java.util.HashMap;
import java.util.Map;

public class LogRiskCalculator implements RiskCalculator {

    @Override
    public Map<SecurityDimension, Float> calculateRisk(Map<SecurityDimension, Float> securityProperties,
                                                       Map<SecurityDimension, Float> consequences) {

        float confidentiality = securityProperties.get(SecurityDimension.CONFIDENTIALITY);
        float confidentialityConsequences = consequences.get(SecurityDimension.CONFIDENTIALITY);
        float riskC = (float) ((1 - confidentiality) * (Math.log10(confidentiality) * confidentialityConsequences));

        float integrity = securityProperties.get(SecurityDimension.INTEGRITY);
        float integrityConsequences = consequences.get(SecurityDimension.INTEGRITY);
        float riskI = (float) ((1 - integrity) * (Math.log10(integrity) * integrityConsequences));

        float availability = securityProperties.get(SecurityDimension.AVAILABILITY);
        float availabilityConsequences = consequences.get(SecurityDimension.AVAILABILITY);
        float riskA = (float) ((1 - availability) * (Math.log10(availability) * availabilityConsequences));

        float trust = securityProperties.get(SecurityDimension.TRUST);
        float trustConsequences = consequences.get(SecurityDimension.TRUST);
        float riskT = (float) ((1 - trust) * (Math.log10(trust) * trustConsequences));

        Map<SecurityDimension, Float> risks = new HashMap<>();
        risks.put(SecurityDimension.CONFIDENTIALITY, riskC);
        risks.put(SecurityDimension.INTEGRITY, riskI);
        risks.put(SecurityDimension.AVAILABILITY, riskA);
        risks.put(SecurityDimension.TRUST, riskT);

        return risks;
    }

}
