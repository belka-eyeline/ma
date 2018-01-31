package mobi.eyeline.utils.general.misc

import groovy.transform.CompileStatic
import org.apache.commons.lang3.StringUtils

@CompileStatic
class UrlUtils {

  static String addParameter(String url, String name, String value) {
    if (StringUtils.isBlank(name)) {
      return url
    }

    value = value ?: ''

    if (containsParameter(url, name)) url = removeParameter(url, name)

    final sep = url.contains("?") ? '&' : '?'

    "$url$sep${encodeString(name)}=${encodeString(value)}"
  }

  static boolean containsParameter(String url, String parameter) {
    StringUtils.contains(url, "&" + parameter + "=") ||
        StringUtils.contains(url, "?" + parameter + "=")
  }

  static String removeParameter(String url, String name) {
    if (!containsParameter(url, name)) return url

    final address = removeAllParameters url
    final parametersMap = getParametersMap url

    parametersMap.remove name

    addParameters address, parametersMap
  }

  static String removeAllParameters(String url) {
    if (url.startsWith("?")) return ""
    int paramIndex = url.indexOf('?')
    if (paramIndex != -1) return url.substring(0, paramIndex)
    else return url
  }

  static String addParameters(String url, Map<String, String> parameters) {
    String result = url
    for (String name : parameters.keySet()) {
      result = addParameter(result, name, parameters.get(name))
    }
    return result
  }

  static boolean containsParameters(String url) {
    return url.indexOf('?') != -1
  }

  static Map<String, String> getParametersMap(String url) {
    Map<String, String> parametersMap = new LinkedHashMap<String, String>()
    if (containsParameters(url)) {
      String parameters = url.substring(url.indexOf('?') + 1)
      String[] nameValues = parameters.split("&")
      for (String nameValue : nameValues) {
        int equalsPos = nameValue.indexOf('=')
        String name = decodeString(nameValue.substring(0, equalsPos))
        String value = decodeString(nameValue.substring(equalsPos + 1))
        parametersMap.put(name, value)
      }
    }
    return parametersMap
  }


  //
  //  Encoding
  //

  private static String encodeString(String _) {
    try { return URLEncoder.encode(_, 'UTF-8') }
    catch (UnsupportedEncodingException ignored) { return _ }
  }

  private static String decodeString(String _) {
    try { return URLDecoder.decode(_, "UTF-8") }
    catch (UnsupportedEncodingException ignored) { return _ }
  }

}
