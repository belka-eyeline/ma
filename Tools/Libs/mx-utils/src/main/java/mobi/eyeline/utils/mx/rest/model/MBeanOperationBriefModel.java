package mobi.eyeline.utils.mx.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.management.MBeanOperationInfo;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class MBeanOperationBriefModel {

  private String name;
  private String description;

  private String href;

  public String getName() {
    return name;
  }

  public MBeanOperationBriefModel setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public MBeanOperationBriefModel setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getHref() {
    return href;
  }

  public MBeanOperationBriefModel setHref(String href) {
    this.href = href;
    return this;
  }

  public MBeanOperationBriefModel fill(final MBeanOperationInfo operation) {
    setName(operation.getName());
    setDescription(operation.getDescription());
    return this;
  }

}
