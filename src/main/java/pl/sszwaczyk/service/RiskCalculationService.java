package pl.sszwaczyk.service;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.domain.SecurityDimension;
import pl.sszwaczyk.domain.security.risk.LogRiskCalculator;
import pl.sszwaczyk.domain.security.risk.RiskCalculator;

import java.util.Collection;
import java.util.Map;

public class RiskCalculationService implements IFloodlightModule, IRiskCalculationService {

    protected static final Logger log = LoggerFactory.getLogger(RiskCalculationService.class);

    private RiskCalculator riskCalculator;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        riskCalculator = new LogRiskCalculator();
        log.info("Risk calculation service initialized with LogRiskCalculator");
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {

    }

    @Override
    public Map<SecurityDimension, Float> calculateRisk(Map<SecurityDimension, Float> securityProperties, Map<SecurityDimension, Float> consequences) {
        Map<SecurityDimension, Float> risks = riskCalculator.calculateRisk(securityProperties, consequences);
        if(log.isDebugEnabled()) {
            log.debug("For security properties: {} and consequences: {}", securityProperties, consequences);
            log.debug("Risks: {}", risks);
        }
        return risks;
    }
}
