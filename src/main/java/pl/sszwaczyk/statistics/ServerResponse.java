package pl.sszwaczyk.statistics;

import lombok.Builder;
import lombok.Data;
import pl.sszwaczyk.routing.solver.Decision;

import java.time.LocalTime;

@Builder
@Data
public class ServerResponse {

    private LocalTime time;
    private String userId;
    private String serviceId;

    private Decision solveResult;

}
