package mobi.eyeline.utils.rest.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides a way to control date pattern in request parameters parsing.
 *
 * @see DateParamConverterProvider Usage guide & implementation
 * @see java.text.SimpleDateFormat Date and Time Patterns
 */
@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.RUNTIME)
public @interface DateParam {

  String value() default "dd.MM.yyyy";
}
