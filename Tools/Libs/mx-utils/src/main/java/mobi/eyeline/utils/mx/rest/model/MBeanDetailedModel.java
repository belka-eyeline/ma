package mobi.eyeline.utils.mx.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class MBeanDetailedModel {

  private String className;
  private String description;

  private List<MBeanAttributeBriefModel> attributes;
  private List<MBeanOperationBriefModel> operations;

  public String getClassName() {
    return className;
  }

  public MBeanDetailedModel setClassName(String className) {
    this.className = className;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public MBeanDetailedModel setDescription(String description) {
    this.description = description;
    return this;
  }

  public List<MBeanAttributeBriefModel> getAttributes() {
    return attributes;
  }

  public MBeanDetailedModel setAttributes(List<MBeanAttributeBriefModel> attributes) {
    this.attributes = attributes;
    return this;
  }

  public List<MBeanOperationBriefModel> getOperations() {
    return operations;
  }

  public MBeanDetailedModel setOperations(List<MBeanOperationBriefModel> operations) {
    this.operations = operations;
    return this;
  }
}
