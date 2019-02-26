package pl.sszwaczyk.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.utils.AddressAndPort;
import pl.sszwaczyk.utils.PacketUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserService implements IFloodlightModule, IUserService {

    protected static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static String DEFAULT_USER_REPOSITORY_FILE = "src/main/resources/repositories/users.json";

    private List<User> users;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s =
                new HashSet<Class<? extends IFloodlightService>>();
        s.add(IUserService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IUserService.class, this);
        return m;
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

    @Override
    public User getUserByIp(String ip) {
        return users.stream().filter(u -> u.getIp().equals(ip))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User getUserFromCntx(FloodlightContext cntx) {
        AddressAndPort addressAndPort = PacketUtils.getDstAddressAndDstTCPPort(cntx);
        if(addressAndPort != null) {
            return getUserByIp(addressAndPort.getAddress());
        }
        return null;
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
