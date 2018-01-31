package mobi.eyeline.utils.mx.rest;

import mobi.eyeline.utils.mx.rest.model.DomainModel;
import mobi.eyeline.utils.mx.rest.model.MBeanBriefModel;

import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
public class DomainEndpoint extends MxEndpoint {

  @GET
  @Path("/domains")
  public List<DomainModel> listDomains() {
    return new ArrayList<DomainModel>() {{
      for (String domain : getMBeanServer().getDomains()) {
        add(new DomainModel().setName(domain));
      }
    }};
  }

  @GET
  @Path("/domains/{domain}")
  public List<MBeanBriefModel> showDomain(@Context final UriInfo uriInfo,
                                          @PathParam("domain") final String domain) {

    return new ArrayList<MBeanBriefModel>() {{

      for (ObjectInstance mBean : getMBeanServer().queryMBeans(null, new MxUtil.DomainQueryExp(domain))) {

        try {
          final ObjectName objectName = mBean.getObjectName();

          final MBeanInfo mbeanInfo = getMBeanServer().getMBeanInfo(objectName);
          final String description = mbeanInfo.getDescription();

          add(
              new MBeanBriefModel()
                  .setObjectName(objectName.toString())
                  .setDescription(description)
                  .setHref(getMethodUri(uriInfo, MBeanEndpoint.class, "showMBean", objectName.toString()).toString())
          );

        } catch (Exception e) {
          log.warn(e.getMessage(), e);
        }
      }

    }};
  }

}
