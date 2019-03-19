package pl.sszwaczyk.routing.solver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.floodlightcontroller.routing.Path;
import pl.sszwaczyk.security.SecurityDimension;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;

import java.time.LocalTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Decision {

    private String id;

    private User user;
    private Service service;

    private Map<SecurityDimension, Float> acceptableRisks;
    private Map<SecurityDimension, Float> maxRisks;

    private LocalTime date;
    private long time; //ms

    private boolean solved;

    //if solved == false
    private Reason reason;

    private double unevenBefore;
    private double unevenAfter;
    private SolveRegion region;
    private double value;
    private Map<SecurityDimension, Float> risks;
    private float risk;

    private int pathLength;
    private long pathLatency;
    private Path path;

}
