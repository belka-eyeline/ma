package mobi.eyeline.utils.mx.rest.model;

import javax.management.MBeanParameterInfo;

public class MBeanParameterModel {

  private String name;
  private String description;

  private String type;

  public String getName() {
    return name;
  }

  public MBeanParameterModel setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public MBeanParameterModel setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getType() {
    return type;
  }

  public MBeanParameterModel setType(String type) {
    this.type = type;
    return this;
  }

  public MBeanParameterModel fill(MBeanParameterInfo mBeanParameterInfo) {
    setName(mBeanParameterInfo.getName());
    setDescription(mBeanParameterInfo.getDescription());
    setType(mBeanParameterInfo.getType());

    return this;
  }
}
