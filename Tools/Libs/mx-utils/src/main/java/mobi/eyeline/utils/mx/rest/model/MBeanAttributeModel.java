package mobi.eyeline.utils.mx.rest.model;


public class MBeanAttributeModel extends MBeanAttributeBriefModel<MBeanAttributeModel> {

  private Object value;

  public Object getValue() {
    return value;
  }

  public MBeanAttributeModel setValue(Object value) {
    this.value = value;
    return this;
  }

}
