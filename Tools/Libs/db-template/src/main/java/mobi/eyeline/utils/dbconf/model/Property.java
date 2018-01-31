package mobi.eyeline.utils.dbconf.model;

import com.google.common.base.MoreObjects;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

/**
 * DB-stored configuration property.
 */
@MappedSuperclass
public abstract class Property {

  @Column(name = "key", nullable = false, unique = true)
  @NotNull @NotBlank
  private String key;

  @Column(name = "value")
  private String value;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("key", getKey())
        .add("value", getValue())
        .toString();
  }
}
