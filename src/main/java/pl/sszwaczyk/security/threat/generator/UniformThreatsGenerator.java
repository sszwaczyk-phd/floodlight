package pl.sszwaczyk.security.threat.generator;

import lombok.Builder;
import lombok.Data;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Data
@Builder
public class UniformThreatsGenerator {

    private static final Logger log = LoggerFactory.getLogger(UniformThreatsGenerator.class);

    private IThreatService threatService;
    private IOFSwitchService switchService;
    private IRoutingService routingService;

    private long seed;

    private int minGap;
    private int maxGap;

    private int minDuration;
    private int maxDuration;

    public void start() {
        Random random = new Random(seed);

        while(true) {
            Threat threat = new Threat();

            List<DatapathId> switches = new ArrayList<>();
            List<DatapathId> dpids = new ArrayList<>(switchService.getAllSwitchDpids());

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
            threat.setSwitches(switches);

            threat.setDuration(random.nextInt((maxDuration - minDuration) + 1) + minDuration);

            log.info("Starting Threat {}", threat);
            threatService.startThreat(threat);

            try {
                int gap = random.nextInt((maxGap - minGap) + 1) + minGap;
                log.info("Sleeping between next threat generation for {} seconds", gap);
                Thread.sleep(gap * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
