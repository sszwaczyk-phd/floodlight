package pl.sszwaczyk.security.properties.web;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import pl.sszwaczyk.security.properties.ISecurityPropertiesService;

import java.util.List;

public class SwitchSecurityPropertiesResource extends ServerResource {

    @Get("switches")
    public List<SwitchSecurityProperties> getSwitchesSecurityProperties() {
        ISecurityPropertiesService propertiesService =
                (ISecurityPropertiesService) getContext().getAttributes().
                        get(ISecurityPropertiesService.class.getCanonicalName());

        return propertiesService.getSwitchesSecurityProperties();
    }

    @Post("switches")
    public void setSwitchSecurityProperties(SwitchSecurityProperties properties) {
        ISecurityPropertiesService propertiesService =
                (ISecurityPropertiesService) getContext().getAttributes().
                        get(ISecurityPropertiesService.class.getCanonicalName());

        propertiesService.setSwitchSecurityProperties(properties);
    }

}
