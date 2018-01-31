package mobi.eyeline.utils.mx.rest.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import javax.management.MBeanAttributeInfo;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public class MBeanAttributeBriefModel<T extends MBeanAttributeBriefModel> {

  private String name;
  private String description;

  private String type;

  private boolean readable;
  private boolean writable;

  private String href;

  public String getName() {
    return name;
  }

  public MBeanAttributeBriefModel setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public MBeanAttributeBriefModel setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getType() {
    return type;
  }

  public MBeanAttributeBriefModel setType(String type) {
    this.type = type;
    return this;
  }

  public boolean isReadable() {
    return readable;
  }

  public MBeanAttributeBriefModel setReadable(boolean readable) {
    this.readable = readable;
    return this;
  }

  public boolean isWritable() {
    return writable;
  }

  public MBeanAttributeBriefModel setWritable(boolean writable) {
    this.writable = writable;
    return this;
  }

  public String getHref() {
    return href;
  }

  public MBeanAttributeBriefModel setHref(String href) {
    this.href = href;
    return this;
  }

  public T fill(MBeanAttributeInfo attr) {
    setName(attr.getName());
    setDescription(attr.getDescription());
    setType(attr.getType());
    setReadable(attr.isReadable());
    setWritable(attr.isWritable());

    //noinspection unchecked
    return (T) this;
  }
}
