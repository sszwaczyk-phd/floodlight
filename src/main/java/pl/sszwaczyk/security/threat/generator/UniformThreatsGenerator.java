package pl.sszwaczyk.security.threat.generator;

import lombok.Builder;
import lombok.Data;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.threat.IThreatService;
import pl.sszwaczyk.security.threat.Threat;
import pl.sszwaczyk.utils.PathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UniformThreatsGenerator implements IThreatGenerator {

    private static final Logger log = LoggerFactory.getLogger(UniformThreatsGenerator.class);

    private final IOFSwitchService switchService;
    private final IRoutingService routingService;

    private final Random random;

    private final int minDuration;
    private final int maxDuration;

    private final boolean onlySwitch;
    private final boolean onlyPath;

    public UniformThreatsGenerator(IOFSwitchService switchService,
                                   IRoutingService routingService,
                                   long seed,
                                   int minDuration,
                                   int maxDuration,
                                   boolean onlySwitch,
                                   boolean onlyPath) {
        this.switchService = switchService;
        this.routingService = routingService;
        this.random = new Random(seed);
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.onlySwitch = onlySwitch;
        this.onlyPath = onlyPath;
    }

    @Override
    public Threat generateThreat() {
        Threat threat = new Threat();

        List<DatapathId> switches = new ArrayList<>();
        List<DatapathId> dpids = new ArrayList<>(switchService.getAllSwitchDpids());

        if(onlySwitch) {
            //One switch attacked
            int switchIndex = random.nextInt(dpids.size());
            switches.add(dpids.get(switchIndex));
        } else if(onlyPath) {
            //Path attacked
            boolean pathFound = false;
            while (!pathFound) {
                int startSwitchIndex = random.nextInt(dpids.size());
                int endSwitchIndex = random.nextInt(dpids.size());

                DatapathId start = dpids.get(startSwitchIndex);
                DatapathId end = dpids.get(endSwitchIndex);

                List<Path> paths = routingService.getPathsSlow(start, end, 10);
                if(paths.size() == 0) {
                    log.warn("Path between " + start + " and " + end + " not found");
                } else {
                    Path path = paths.get(random.nextInt(paths.size()));
                    switches.addAll(PathUtils.getSwitchesFromPath(path));
                    pathFound = true;
                }
            }
        } else {
            //random switch or path
            int switchOrPath = random.nextInt(2);
            if(switchOrPath == 0) {
                //One switch attacked
                int switchIndex = random.nextInt(dpids.size());
                switches.add(dpids.get(switchIndex));
            } else {
                //Path attacked
                boolean pathFound = false;
                while (!pathFound) {
                    int startSwitchIndex = random.nextInt(dpids.size());
                    int endSwitchIndex = random.nextInt(dpids.size());

                    DatapathId start = dpids.get(startSwitchIndex);
                    DatapathId end = dpids.get(endSwitchIndex);

                    List<Path> paths = routingService.getPathsSlow(start, end, 10);
                    if(paths.size() == 0) {
                        log.warn("Path between " + start + " and " + end + " not found");
                    } else {
                        Path path = paths.get(random.nextInt(paths.size()));
                        switches.addAll(PathUtils.getSwitchesFromPath(path));
                        pathFound = true;
                    }
                }
            }
        }
        threat.setSwitches(switches);

        threat.setDuration(random.nextInt((maxDuration - minDuration) + 1) + minDuration);

        return threat;
    }
}
