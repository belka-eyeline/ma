package mobi.eyeline.utils.rest.inject;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.glassfish.jersey.server.model.Parameter.Source.ENTITY;
import static org.glassfish.jersey.server.model.Parameter.Source.UNKNOWN;

/**
 * Same as {@linkplain javax.ws.rs.QueryParam}, but:
 * <ol>
 *   <li>
 *     Allows setting multiple variable names, thus implementing aliases for input parameters.
 *     This way one can pass request parameter using any of declared names.
 *   </li>
 *   <li>
 *     For now is restricted to {@linkplain String} injections only. Custom converters
 *     won't work either.
 *   </li>
 * </ol>
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryParams {

  /**
   * Allowed parameter names.
   * In case multiple ones are set, the value of the first one declared is used.
   */
  String[] value();


  @Singleton
  class Resolver {

    public static class QueryParamsInjectionResolver extends ParamInjectionResolver<QueryParams> {
      public QueryParamsInjectionResolver() {
        super(QueryParamsValueFactoryProvider.class);
      }
    }

    @Singleton
    public static class QueryParamsValueFactoryProvider extends AbstractValueFactoryProvider {
      @Inject
      public QueryParamsValueFactoryProvider(MultivaluedParameterExtractorProvider mpep,
                                             ServiceLocator injector) {
        super(mpep, injector, UNKNOWN, ENTITY);
      }

      @Override
      protected Factory<?> createValueFactory(final Parameter parameter) {
        if (parameter.getRawType() != String.class) {
          return null;
        }

        final String[] parameterNames;
        {
          final QueryParams qp = parameter.getAnnotation(QueryParams.class);
          if (qp == null) {
            return null;
          }
          parameterNames = qp.value();
        }

        return new AbstractContainerRequestValueFactory<String>() {
          @Override
          public String provide() {
            final MultivaluedMap<String, String> queryParams =
                getContainerRequest().getUriInfo().getQueryParameters(!parameter.isEncoded());

            for (String param : parameterNames) {
              final String value = queryParams.getFirst(param);
              if (!isNullOrEmpty(value)) {
                return value;
              }
            }

            return parameter.getDefaultValue();
          }
        };
      }
    }

    public static class Binder extends AbstractBinder {

      @Override
      protected void configure() {
        bind(QueryParamsValueFactoryProvider.class)
            .to(ValueFactoryProvider.class)
            .in(Singleton.class);
        bind(QueryParamsInjectionResolver.class)
            .to(new TypeLiteral<InjectionResolver<QueryParams>>() {})
            .in(Singleton.class);
      }
    }
  }
}
