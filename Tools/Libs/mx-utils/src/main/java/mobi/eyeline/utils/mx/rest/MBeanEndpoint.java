package mobi.eyeline.utils.mx.rest;

import mobi.eyeline.utils.mx.rest.model.MBeanAttributeBriefModel;
import mobi.eyeline.utils.mx.rest.model.MBeanAttributeModel;
import mobi.eyeline.utils.mx.rest.model.MBeanDetailedModel;
import mobi.eyeline.utils.mx.rest.model.MBeanOperationBriefModel;
import mobi.eyeline.utils.mx.rest.model.MBeanOperationModel;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static mobi.eyeline.utils.rest.errors.ErrorUtils.badRequest;
import static mobi.eyeline.utils.rest.errors.ErrorUtils.notFound;

@Produces(MediaType.APPLICATION_JSON)
public class MBeanEndpoint extends MxEndpoint {

  @GET
  @Path("/beans/{objectName}")
  public MBeanDetailedModel showMBean(@Context final UriInfo uriInfo,
                                      @PathParam("objectName") final String objectNameString) {

    final ObjectName objectName = getObjectName(objectNameString);
    final MBeanInfo mbeanInfo = getMBeanInfo(objectName);

    return new MBeanDetailedModel()
        .setClassName(mbeanInfo.getClassName())
        .setDescription(mbeanInfo.getDescription())
        .setAttributes(
            new ArrayList<MBeanAttributeBriefModel>() {{
              for (MBeanAttributeInfo attr : mbeanInfo.getAttributes()) {
                add(
                    new MBeanAttributeBriefModel()
                        .fill(attr)
                        .setHref(
                            getMethodUri(
                                uriInfo,
                                MBeanEndpoint.class,
                                "getAttribute",
                                objectNameString,
                                attr.getName()
                            ).toString()
                        )
                );
              }
            }})
        .setOperations(
            new ArrayList<MBeanOperationBriefModel>() {{
              for (MBeanOperationInfo operation : mbeanInfo.getOperations()) {
                if (MxUtil.isPropertyAccessor(operation)) continue;

                add(
                    new MBeanOperationBriefModel()
                        .fill(operation)
                        .setHref(
                            getMethodUri(
                                uriInfo,
                                MBeanEndpoint.class,
                                "getOperation",
                                objectNameString,
                                operation.getName()
                            ).toString()
                        )
                );
              }
            }});
  }

  @GET
  @Path("/beans/{objectName}/attributes/{attributeName}/value")
  public Object getAttributeValue(@PathParam("objectName") String objectNameString,
                                  @PathParam("attributeName") String attributeName) {

    final ObjectName objectName = getObjectName(objectNameString);

    try {
      return getMBeanServer().getAttribute(objectName, attributeName);

    } catch (AttributeNotFoundException e) {
      throw notFound("NOT_FOUND", "Attribute not found");

    } catch (Exception e) {
      throw badRequest("BAD_REQUEST", "Failed obtaining attribute");
    }
  }

  @GET
  @Path("/beans/{objectName}/attributes/{attributeName}")
  public MBeanAttributeModel getAttribute(

      @PathParam("objectName") final String objectNameString,
      @PathParam("attributeName") final String attributeName) {

    final ObjectName objectName = getObjectName(objectNameString);
    final MBeanInfo mbeanInfo = getMBeanInfo(objectName);

    final MBeanAttributeInfo mBeanAttributeInfo = MxUtil.getMBeanAttributeInfo(mbeanInfo, attributeName);
    if (mBeanAttributeInfo == null) {
      throw notFound("NOT_FOUND", "Attribute not found");
    }

    return new MBeanAttributeModel()
        .setValue(getAttributeValue(objectNameString, attributeName))
        .fill(mBeanAttributeInfo);
  }

  @POST
  @Path("/beans/{objectName}/attributes/{attributeName}/value")
  public MBeanAttributeModel setAttributeValue(@PathParam("objectName") final String objectNameString,
                                               @PathParam("attributeName") final String attributeName,
                                               final String valueString) {

    final ObjectName objectName = getObjectName(objectNameString);
    final MBeanInfo mbeanInfo = getMBeanInfo(objectName);

    final MBeanAttributeInfo mBeanAttributeInfo = MxUtil.getMBeanAttributeInfo(mbeanInfo, attributeName);
    if (mBeanAttributeInfo == null) {
      throw notFound("NOT_FOUND", "Attribute not found");
    }

    final Object value;
    try {
      value = MxUtil.fromString(valueString, mBeanAttributeInfo.getType());

    } catch (Exception e) {
      throw badRequest("BAD_REQUEST", "Invalid attribute value");
    }

    try {
      getMBeanServer().setAttribute(objectName, new Attribute(attributeName, value));

    } catch (InstanceNotFoundException e) {
      throw notFound("NOT_FOUND", "Bean not found");

    } catch (AttributeNotFoundException e) {
      throw notFound("NOT_FOUND", "Attribute not found");

    } catch (InvalidAttributeValueException e) {
      throw notFound("NOT_FOUND", "Invalid attribute value");

    } catch (Exception e) {
      log.warn("Failed setting attribute value," +
          " objectName = [" + objectName + "], value = [" + valueString + "]", e);
      throw badRequest("BAD_REQUEST", "Failed setting attribute value");
    }

    return getAttribute(objectNameString, attributeName);
  }

  @GET
  @Path("/beans/{objectName}/operations/{operationName}")
  public MBeanOperationModel getOperation(@PathParam("objectName") final String objectNameString,
                                          @PathParam("operationName") final String operationName) {

    final ObjectName objectName = getObjectName(objectNameString);
    final MBeanInfo mbeanInfo = getMBeanInfo(objectName);

    final MBeanOperationInfo mBeanOperationInfo = MxUtil.getMBeanOperationInfo(mbeanInfo, operationName);
    if (mBeanOperationInfo == null) {
      throw notFound("NOT_FOUND", "Operation not found");
    }

    return new MBeanOperationModel().fill(mBeanOperationInfo);
  }

  @POST
  @Path("/beans/{objectName}/operations/{operationName}")
  public Object invokeOperation(@PathParam("objectName") final String objectNameString,
                                @PathParam("operationName") final String operationName,
                                Object[] parameters) {

    final ObjectName objectName = getObjectName(objectNameString);
    final MBeanInfo mbeanInfo = getMBeanInfo(objectName);

    final MBeanOperationInfo mBeanOperationInfo = MxUtil.getMBeanOperationInfo(mbeanInfo, operationName);
    if (mBeanOperationInfo == null) {
      throw notFound("NOT_FOUND", "Operation not found");
    }

    final List<String> paramTypes = new ArrayList<String>() {{
      final MBeanParameterInfo[] signature = mBeanOperationInfo.getSignature();
      for (MBeanParameterInfo mBeanParameterInfo : signature) {
        add(mBeanParameterInfo.getType());
      }
    }};

    try {
      return getMBeanServer().invoke(
          objectName,
          operationName,
          parameters,
          paramTypes.toArray(new String[0])
      );

    } catch (Exception e) {
      log.warn("Failed invoking "
          + objectNameString + "#" + operationName + "(" + Arrays.toString(parameters) + ")", e);
      throw new WebApplicationException("Failed invoking operation");
    }
  }

}
