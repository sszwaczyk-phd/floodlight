package pl.sszwaczyk.security.threat.generator;

import lombok.Builder;
import lombok.Data;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.threat.IThreatService;
import pl.sszwaczyk.security.threat.Threat;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Data
@Builder
public class UniformThreatsGenerator {

    private static final Logger log = LoggerFactory.getLogger(UniformThreatsGenerator.class);

    private IThreatService threatService;
    private IOFSwitchService switchService;
    private ILinkDiscoveryService linkService;

    private int minGap;
    private int maxGap;

    private int minDuration;
    private int maxDuration;

    public void start() {
        Random random = new Random();

        while(true) {
            Threat threat = new Threat();

            int linkOrSwitch = random.nextInt(2);
            int index = 0;

            if(linkOrSwitch == 0) {
                Set<Link> links = linkService.getLinks().keySet();
                int linkIndex = random.nextInt(linkService.getLinks().size());
                for(Link l: links) {
                    if(index == linkIndex) {
                        threat.setSrc(l.getSrc());
                        threat.setSrcPort(l.getSrcPort());
                        threat.setDst(l.getDst());
                        threat.setDstPort(l.getDstPort());
                        break;
                    }
                    index++;
                }
            } else {
                Set<DatapathId> switchDpids = switchService.getAllSwitchDpids();
                int switchIndex = random.nextInt(switchDpids.size());
                for(DatapathId dpid: switchDpids) {
                    if(index == switchIndex) {
                        threat.setSrc(dpid);
                    }
                    index++;
                }
            }

            threat.setDuration(ThreadLocalRandom.current().nextLong(minDuration, maxDuration + 1));

            log.info("Starting Threat {}", threat);
            threatService.startThreat(threat);

            try {
                long gap = ThreadLocalRandom.current().nextLong(minGap * 1000, maxGap * 1000 + 1);
                log.info("Sleeping between next threat generation for {} seconds", (gap / 1000));
                Thread.sleep(gap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
