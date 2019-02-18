package pl.sszwaczyk.security.threat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Threat {

    private String id;
    private ThreatType type;
    private DatapathId src;
    private OFPort srcPort;
    private DatapathId dst;
    private OFPort dstPort;

}
