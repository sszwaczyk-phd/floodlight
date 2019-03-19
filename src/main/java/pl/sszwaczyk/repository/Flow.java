package pl.sszwaczyk.repository;

import lombok.Builder;
import lombok.Data;
import pl.sszwaczyk.routing.solver.Decision;
import pl.sszwaczyk.service.Service;
import pl.sszwaczyk.user.User;
import pl.sszwaczyk.utils.AddressesAndPorts;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class Flow {

    private LocalTime startTime;
    private User user;
    private Service service;
    private AddressesAndPorts ap;
    private FlowStatus flowStatus;

    private List<Decision> decisions;

}
