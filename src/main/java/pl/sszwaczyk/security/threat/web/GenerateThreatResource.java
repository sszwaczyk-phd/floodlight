package pl.sszwaczyk.security.threat.web;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.sszwaczyk.security.threat.IThreatService;
import pl.sszwaczyk.security.threat.Threat;

import java.util.UUID;
import java.util.stream.Collectors;

public class GenerateThreatResource extends ServerResource {

    protected static Logger log = LoggerFactory.getLogger(GenerateThreatResource.class);

    @Post("generate")
    public void generateThreat(GenerateThreatDTO generateThreatDTO) {
        IThreatService threatService =
                (IThreatService) getContext().getAttributes().
                        get(IThreatService.class.getCanonicalName());

        log.info("Generating threat from REST API.");
        log.debug("Got threat generation request {}", generateThreatDTO);

        Threat threat = new Threat();
        threat.setId(UUID.randomUUID().toString());
        threat.setSwitches(generateThreatDTO.getSwitches().stream().map(DatapathId::of).collect(Collectors.toList()));
        threat.setDuration(generateThreatDTO.getDuration());

        threatService.startThreat(threat);
    }
}
