package mobi.eyeline.utils.mx.rest.model;

public class MBeanBriefModel {

  private String objectName;
  private String description;
  private String href;

  public String getObjectName() {
    return objectName;
  }

  public MBeanBriefModel setObjectName(String objectName) {
    this.objectName = objectName;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public MBeanBriefModel setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getHref() {
    return href;
  }

  public MBeanBriefModel setHref(String href) {
    this.href = href;
    return this;
  }
}
