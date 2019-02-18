package pl.sszwaczyk.user;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {

    private String id;
    private String ip;
    private String datapathId;

}
