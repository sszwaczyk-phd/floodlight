package pl.sszwaczyk.security.threat.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateThreatDTO {

    private List<String> switches;

    private int duration; //seconds

}
