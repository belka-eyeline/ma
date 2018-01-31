package mobi.eyeline.utils.rest.inject;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.tryFind;
import static java.util.Arrays.asList;

/**
 * Implementation of {@linkplain DateParam}.
 *
 * <h1>Registration</h1>
 *
 * <pre>
 *   class JaxRsApplication extends ResourceConfig {
 *
 *     JaxRsApplication() {
 *       register DateParamConverterProvider
 *
 *       register new AbstractBinder() {
 *         protected void configure() {
 *           bind(DateParamConverterProvider)
 *               .to(ParamConverterProvider.class)
 *               .ranked(10)
 *         }
 *       }
 *
 *     }
 *   }
 * </pre>
 *
 * <h1>Sample usage</h1>
 * <pre>
 *   Response foo(@QueryParam('modifiedFrom') @DateParam('dd.MM.yyyy') Date from) {
 *     ...
 *   }
 * </pre>
 */
@Provider
public class DateParamConverterProvider implements ParamConverterProvider {

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType,
                                            Type genericType,
                                            Annotation[] annotations) {

    if (!Date.class.equals(rawType)) {
      return null;
    }

    final Optional<Annotation> dateParam = tryFind(
        asList(annotations),
        new Predicate<Annotation>() {
          @Override
          public boolean apply(Annotation _) { return _.annotationType() == DateParam.class; }
        });

    if (!dateParam.isPresent()) {
      return null;
    }

    final String fmt = ((DateParam) dateParam.get()).value();

    return new ParamConverter<T>() {
      final SimpleDateFormat sdf = new SimpleDateFormat(fmt);

      @Override
      public T fromString(String value) {
        checkArgument(value != null);

        try {
          //noinspection unchecked
          return (T) sdf.parse(value);

        } catch (Exception e) {
          throw new IllegalArgumentException(e);
        }
      }

      @Override
      public String toString(T value) {
        checkArgument(value != null);

        try {
          return sdf.format(value);

        } catch (Exception e) {
          throw new IllegalArgumentException(e);
        }

      }
    };
  }

}
