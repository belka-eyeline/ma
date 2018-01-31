package mobi.eyeline.utils.mx.rest;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;
import java.lang.reflect.Constructor;

class MxUtil {

  static boolean isPropertyAccessor(MBeanOperationInfo operationInfo) {
    final String name = operationInfo.getName();
    return name.startsWith("is") || name.startsWith("get") || name.startsWith("set");
  }

  static Object fromString(String value, String type) throws IllegalArgumentException {
    if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
      return Boolean.parseBoolean(value);

    } else if (type.equals("char") || type.equals("java.lang.Character")) {
      return value.length() == 0 ? '\0' : value.toCharArray()[0];

    } else if (type.equals("byte") || type.equals("java.lang.Byte")) {
      return Byte.parseByte(value);

    } else if (type.equals("short") || type.equals("java.lang.Short")) {
      return Short.parseShort(value);

    } else if (type.equals("int") || type.equals("java.lang.Integer")) {
      return Integer.parseInt(value);

    } else if (type.equals("long") || type.equals("java.lang.Long")) {
      return Long.parseLong(value);

    } else if (type.equals("java.lang.String")) {
      return value;

    } else if (type.equals("float") || type.equals("java.lang.Float")) {
      return Float.parseFloat(value);

    } else if (type.equals("double") || type.equals("java.lang.Double")) {
      return Double.parseDouble(value);

    } else {
      try {
        return getConstructor(type).newInstance(value);

      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Cannot instantiate [" + type + "] from [" + value + "]");
      }
    }
  }

  private static <C> Constructor<C> getConstructor(String typeString) {
    final Class<Object> clazz;
    try {
      //noinspection unchecked
      clazz = (Class<Object>) Class.forName(typeString);

    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Unknown class for type [" + typeString + "]");
    }

    try {
      //noinspection unchecked
      return (Constructor<C>) clazz.getConstructor(String.class);

    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot find <init>(Ljava.lang.String;) in [" + clazz + "]");
    }
  }

  static MBeanAttributeInfo getMBeanAttributeInfo(MBeanInfo mBeanInfo,
                                                  String attributeName) {

    for (MBeanAttributeInfo mBeanAttributeInfo : mBeanInfo.getAttributes()) {
      if (mBeanAttributeInfo.getName().equals(attributeName)) return mBeanAttributeInfo;
    }

    return null;
  }

  static MBeanOperationInfo getMBeanOperationInfo(MBeanInfo mBeanInfo,
                                                  String operationName) {

    for (MBeanOperationInfo mBeanOperationInfo : mBeanInfo.getOperations()) {
      if (mBeanOperationInfo.getName().equals(operationName)) return mBeanOperationInfo;
    }

    return null;
  }

  /**
   * Filters objects by the specified domain name.
   */
  static class DomainQueryExp implements QueryExp {

    private final String domain;

    DomainQueryExp(String domain) {
      this.domain = domain;
    }

    @Override
    public void setMBeanServer(MBeanServer s) {
      /* Nothing here */
    }

    @Override
    public boolean apply(ObjectName name) {
      return domain.equals(name.getDomain());
    }
  }
}
