package pl.sszwaczyk.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.domain.User;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserRepository implements IFloodlightModule {

    protected static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private static String DEFAULT_USER_REPOSITORY_FILE = "src/main/resources/repositories/users.json";

    private List<User> users;

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
            DEFAULT_USER_REPOSITORY_FILE = tmp;
            log.info("Default users repository file set to {}.", DEFAULT_USER_REPOSITORY_FILE);
        } else {
            log.info("Default users repository not configured. Using {}.", DEFAULT_USER_REPOSITORY_FILE);
        }

        try {
            loadUsers();
        } catch (IOException e) {
            log.error("Cannot read users from file because {}.", e.getMessage());
            throw new FloodlightModuleException("Cannot init Users Repository!");
        }
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {

    }

    private User findUserByIP(String ip) {
        return users.stream().filter(u -> u.getIp().equals(ip))
                .findFirst()
                .orElse(null);
    }

    private void loadUsers() throws IOException {
        File usersFile = new File(DEFAULT_USER_REPOSITORY_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        users = objectMapper.readValue(usersFile, new TypeReference<List<User>>(){});

        if(log.isDebugEnabled()) {
            log.debug("Loaded users:");
            for (User user: users) {
                log.debug(user.toString());
            }
        }
    }
}
