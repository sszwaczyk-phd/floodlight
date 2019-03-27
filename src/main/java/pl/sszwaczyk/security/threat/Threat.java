package pl.sszwaczyk.security.threat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projectfloodlight.openflow.types.DatapathId;

import java.time.LocalTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Threat {

    private String id;
    List<DatapathId> switches;

    private LocalTime startTime;
    private Integer duration; //seconds
}
