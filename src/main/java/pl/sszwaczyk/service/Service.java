package pl.sszwaczyk.service;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Service {

    private String id;
    private String ip;
    private Integer port;
    private String path;
    private Double bandwidth;

}
