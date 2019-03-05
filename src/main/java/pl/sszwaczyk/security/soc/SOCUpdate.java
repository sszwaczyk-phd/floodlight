package pl.sszwaczyk.security.soc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import pl.sszwaczyk.security.SecurityDimension;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SOCUpdate {

    private SOCUpdateType type;
    private List<DatapathId> switches;
    private Map<SecurityDimension, Float> securityPropertiesDifference;

}
