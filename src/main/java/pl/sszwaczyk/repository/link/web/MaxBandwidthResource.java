package pl.sszwaczyk.repository.link.web;

import net.floodlightcontroller.statistics.SwitchPortBandwidth;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.repository.link.ILinkStatisticsRepository;
import pl.sszwaczyk.repository.link.MaxLinkUtilization;

import java.util.List;

public class MaxBandwidthResource extends ServerResource {

    @Get("max-bandwidth")
    public List<MaxLinkUtilization> getMaxLinksBandwidth() {
        ILinkStatisticsRepository linkStatisticsRepository =
                (ILinkStatisticsRepository) getContext().getAttributes().
                        get(ILinkStatisticsRepository.class.getCanonicalName());

        return linkStatisticsRepository.getMaxLinksBandwidth();
    }

}
