package pl.sszwaczyk.repository.link;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaxLinkUtilization {

    private DatapathId id;
    private OFPort pt;

    private double maxRxUtilization;
    private double maxRxUtilizationPercent;

    private double maxTxUtilization;
    private double maxTxUtilizationPercent;


}
