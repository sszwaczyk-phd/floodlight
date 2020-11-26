package pl.sszwaczyk.repository.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkUtilizationAtTime {

    private LocalTime date;

    private DatapathId datapathId;
    private OFPort pt;

    private double rxUtilization;
    private double rxUtilizationPercent;

    private double txUtilization;
    private double txUtilizationPercent;

}
