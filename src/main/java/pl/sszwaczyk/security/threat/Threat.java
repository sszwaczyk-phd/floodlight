package pl.sszwaczyk.security.threat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Threat {

    private String id;
    List<DatapathId> switches;

    private Long duration; //seconds
}
