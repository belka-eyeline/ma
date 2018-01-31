package mobi.eyeline.utils.rest.marshal;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.apache.commons.lang3.StringUtils;

public class PrefixNamingStrategy extends PropertyNamingStrategy {

  @Override
  public String nameForField(MapperConfig<?> config,
                             AnnotatedField field,
                             String defaultName) {

    final String name = translate(config, field, defaultName);
    return name == null ? super.nameForField(config, field, defaultName) : name;
  }

  @Override
  public String nameForGetterMethod(MapperConfig<?> config,
                                    AnnotatedMethod method,
                                    String defaultName) {
    final String name = translate(config, method, defaultName);
    return name == null ? super.nameForGetterMethod(config, method, defaultName) : name;
  }

  @Override
  public String nameForSetterMethod(MapperConfig<?> config,
                                    AnnotatedMethod method,
                                    String defaultName) {
    final String name = translate(config, method, defaultName);
    return name == null ? super.nameForSetterMethod(config, method, defaultName) : name;
  }

  private String translate(MapperConfig<?> config, AnnotatedMember field, String defaultName) {
    final PropertyPrefix prefix =
        field.getDeclaringClass().getAnnotation(PropertyPrefix.class);
    if (prefix == null) {
      return null;
    }

    if (prefix.capitalize()) {

      defaultName = StringUtils.capitalize(defaultName);
    }

    return prefix.value() + defaultName;
  }


}
