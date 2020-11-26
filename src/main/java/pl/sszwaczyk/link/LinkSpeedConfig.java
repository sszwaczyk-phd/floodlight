package pl.sszwaczyk.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkSpeedConfig {

    private String datapathId;
    private Integer portNumber;
    private Long speed; //kb/s

}
