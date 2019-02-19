package pl.sszwaczyk.service;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Service {

    private String id;
    private String ip;
    private Long port;
    private String path;

}
