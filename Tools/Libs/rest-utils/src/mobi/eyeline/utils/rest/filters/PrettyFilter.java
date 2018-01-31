package mobi.eyeline.utils.rest.filters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;
import org.apache.commons.lang3.BooleanUtils;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * Enable pretty-printing of JSON response in case query parameters contain {@code pretty = true}.
 */
public class PrettyFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext req,
                     ContainerResponseContext resp) throws IOException {

    final MultivaluedMap<String, String> queryParameters = req
        .getUriInfo()
        .getQueryParameters();

    if (BooleanUtils.toBoolean(queryParameters.getFirst("pretty"))) {
      ObjectWriterInjector.set(new IndentationModifier(true));
    }
  }

  private static class IndentationModifier extends ObjectWriterModifier {

    private final boolean indent;

    IndentationModifier(boolean indent) { this.indent = indent; }

    @Override
    public ObjectWriter modify(EndpointConfigBase<?> endpointConfigBase,
                               MultivaluedMap<String, Object> multivaluedMap,
                               Object o,
                               ObjectWriter objectWriter,
                               JsonGenerator jsonGenerator) throws IOException {
      if (indent) {
        jsonGenerator.useDefaultPrettyPrinter();
      }
      return objectWriter;
    }
  }
}