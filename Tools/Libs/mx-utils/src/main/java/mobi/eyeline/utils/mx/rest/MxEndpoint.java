package mobi.eyeline.utils.mx.rest;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.glassfish.jersey.server.model.Resource;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static mobi.eyeline.utils.rest.errors.ErrorUtils.badRequest;
import static mobi.eyeline.utils.rest.errors.ErrorUtils.notFound;

public abstract class MxEndpoint {

  final Logger log = Logger.getLogger(MBeanEndpoint.class);

  private final MBeanServer mbeanServer;

  MxEndpoint() {
    mbeanServer = ManagementFactory.getPlatformMBeanServer();
  }

  @SuppressWarnings("WeakerAccess")
  public static Set<Resource> resources(String prefix) {
    return new HashSet<Resource>(asList(

        Resource.builder(DomainEndpoint.class).path(prefix).build(),
        Resource.builder(MBeanEndpoint.class).path(prefix).build()

    ));
  }

  MBeanServer getMBeanServer() {
    return mbeanServer;
  }

  @SuppressWarnings("WeakerAccess")
  public static Set<Resource> resources() {
    return resources("mx");
  }


  //
  //
  //

  private UriBuilder getRootEndpointUri(UriInfo uriInfo) {
    final String mxEndpointsPrefix =
        ((UriRoutingContext) uriInfo).getMatchedModelResource().getParent().getPath();
    return UriBuilder.fromUri(uriInfo.getBaseUri()).path(mxEndpointsPrefix);
  }

  URI getMethodUri(UriInfo uriInfo, Class resource, String method, Object... values) {
    return getRootEndpointUri(uriInfo).path(resource, method).build(values);
  }

  MBeanInfo getMBeanInfo(ObjectName objectName) {
    final MBeanInfo mbeanInfo;
    try {
      mbeanInfo = mbeanServer.getMBeanInfo(objectName);

    } catch (Exception e) {
      log.warn("Failed getting MBean info by objectName = [" + objectName + "]", e);
      throw notFound("NOT_FOUND", "Bean not found");
    }
    return mbeanInfo;
  }

  ObjectName getObjectName(String objectNameString) {
    try {
      return new ObjectName(objectNameString);

    } catch (MalformedObjectNameException e) {
      throw badRequest("BAD_REQUEST", "Invalid object name");
    }
  }
}
