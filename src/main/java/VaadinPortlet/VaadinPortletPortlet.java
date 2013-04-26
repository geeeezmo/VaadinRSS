package VaadinPortlet;

/**
 * Created with IntelliJ IDEA.
 * User: Igor.Borisov
 * Date: 05.04.13
 * Time: 12:31
 */

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.VaadinPortlet;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;

public class VaadinPortletPortlet extends VaadinPortlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected VaadinPortletService createPortletService(
            DeploymentConfiguration deploymentConfiguration) {
        return new VaadinPortletService(this, deploymentConfiguration) {
            private static final long serialVersionUID = 1L;
            @Override
            public final String getStaticFileLocation(VaadinRequest request) {
                // Use static resources (widgetsets and themes) in the portlet war:
                return request.getContextPath();
            }
        };
    }
}