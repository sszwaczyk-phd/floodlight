package pl.sszwaczyk.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.domain.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ServiceRepository implements IFloodlightModule {

    protected static final Logger log = LoggerFactory.getLogger(ServiceRepository.class);

    private static String DEFAULT_SERVICE_REPOSITORY_FILE = "src/main/resources/repositories/services.json";

    private List<Service> services;

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
        Map<String, String> configParameters = context.getConfigParams(this);
        String tmp = configParameters.get("repository-file");
        if (tmp != null) {
            DEFAULT_SERVICE_REPOSITORY_FILE = tmp;
            log.info("Default services repository file set to {}.", DEFAULT_SERVICE_REPOSITORY_FILE);
        } else {
            log.info("Default services repository not configured. Using {}.", DEFAULT_SERVICE_REPOSITORY_FILE);
        }

        try {
            loadServices();
        } catch (IOException e) {
            log.error("Cannot read services from file because {}.", e.getMessage());
            throw new FloodlightModuleException("Cannot init Services Repository!");
        }
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {

    }

    public Service getServiceById(String id) {
        return services.stream().filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void loadServices() throws IOException {
        File usersFile = new File(DEFAULT_SERVICE_REPOSITORY_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        services = objectMapper.readValue(usersFile, new TypeReference<List<Service>>(){});

        if(log.isDebugEnabled()) {
            log.debug("Loaded services:");
            for (Service service: services) {
                log.debug(service.toString());
            }
        }
    }
}
