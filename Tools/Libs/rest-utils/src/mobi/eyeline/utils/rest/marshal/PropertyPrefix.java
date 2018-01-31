package mobi.eyeline.utils.rest.marshal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a common prefix prepended to all of the object properties during serialization.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PropertyPrefix {

  String value();

  boolean capitalize() default false;

}
