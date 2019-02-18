package pl.sszwaczyk.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.domain.DTSP;
import pl.sszwaczyk.domain.Service;
import pl.sszwaczyk.domain.User;
import pl.sszwaczyk.json.DTSPJson;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DTSPService implements IFloodlightModule, IDTSPService {

    protected static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static String DEFAULT_DTSP_REPOSITORY_FILE = "src/main/resources/repositories/dtsp.json";

    private Map<Service, DTSP> dtsps;

    private IServiceService serviceService;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(IDTSPService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IDTSPService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IServiceService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        serviceService = context.getServiceImpl(IServiceService.class);

        Map<String, String> configParameters = context.getConfigParams(this);
        String tmp = configParameters.get("repository-file");
        if (tmp != null) {
            DEFAULT_DTSP_REPOSITORY_FILE = tmp;
            log.info("Default DTSP repository file set to {}.", DEFAULT_DTSP_REPOSITORY_FILE);
        } else {
            log.info("Default DTSP repository not configured. Using {}.", DEFAULT_DTSP_REPOSITORY_FILE);
        }

        try {
            loadDtsps();
        } catch (IOException e) {
            log.error("Cannot read dtsp from file because {}.", e.getMessage());
            throw new FloodlightModuleException("Cannot init DTSP Repository!");
        }
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {

    }

    @Override
    public DTSP getDTSPForService(Service service) {
        return dtsps.get(service);
    }

    private void loadDtsps() throws IOException, FloodlightModuleException {
        dtsps = new HashMap<>();

        File dtspFile = new File(DEFAULT_DTSP_REPOSITORY_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        List<DTSPJson> dtspJsons = objectMapper.readValue(dtspFile, new TypeReference<List<DTSPJson>>(){});

        for(DTSPJson dtspJson: dtspJsons) {
            Service service = serviceService.getServiceById(dtspJson.getServiceId());
            if(service == null) {
                throw new FloodlightModuleException("Service " + dtspJson.getServiceId() + " not found!");
            }
            DTSP dtsp = DTSP.builder()
                    .service(service)
                    .requirements(dtspJson.getRequirements())
                    .consequences(dtspJson.getConsequences())
                    .acceptableRiskIncrease(dtspJson.getAcceptableRiskIncrease())
                    .build();
            dtsps.put(service, dtsp);
        }

        if(log.isDebugEnabled()) {
            log.debug("Loaded dtsps:");
            dtsps.entrySet().stream()
                    .forEach(entry -> log.debug(entry.getValue().toString()));
        }
    }

}
