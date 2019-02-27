package pl.sszwaczyk.security.properties;

import lombok.Builder;
import lombok.Data;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.linkdiscovery.Link;

@Data
@Builder
public class SecurityPropertiesUpdate {

    private SecurityPropertiesUpdateType type;
    private Link link;
    private IOFSwitch iofSwitch;

}
