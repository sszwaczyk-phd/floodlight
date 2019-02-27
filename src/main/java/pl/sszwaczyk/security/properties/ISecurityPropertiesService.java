package pl.sszwaczyk.security.properties;

import net.floodlightcontroller.core.module.IFloodlightService;
import pl.sszwaczyk.security.properties.web.LinkSecurityProperties;
import pl.sszwaczyk.security.properties.web.SwitchSecurityProperties;

import java.util.List;

public interface ISecurityPropertiesService extends IFloodlightService {

    List<SwitchSecurityProperties> getSwitchesSecurityProperties();

    List<LinkSecurityProperties> getLinksSecurityProperties();

    void setSwitchSecurityProperties(SwitchSecurityProperties properties);

    void setLinkSecurityProperties(LinkSecurityProperties properties);

    void addListener(ISecurityPropertiesChangedListener listener);
}
