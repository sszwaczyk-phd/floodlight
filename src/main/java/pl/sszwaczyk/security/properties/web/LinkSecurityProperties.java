package pl.sszwaczyk.security.properties.web;

import lombok.Data;

@Data
public class LinkSecurityProperties {

    private String src;
    private Integer srcPort;
    private String dst;
    private Integer dstPort;
    private Float confidentiality;
    private Float integrity;
    private Float availability;

}
