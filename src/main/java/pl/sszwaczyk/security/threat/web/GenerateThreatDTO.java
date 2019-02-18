package pl.sszwaczyk.security.threat.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateThreatDTO {

    private String src;
    private int srcPort;
    private String dst;
    private int dstPort;

    private long duration; //seconds

}
