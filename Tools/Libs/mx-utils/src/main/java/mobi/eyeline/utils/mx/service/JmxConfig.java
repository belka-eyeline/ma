package mobi.eyeline.utils.mx.service;

public interface JmxConfig {

  /** If disabled, JMX server won't be started. */
  boolean isJmxEnabled();

  /** Port to bind server to, mandatory if enabled */
  int getJmxPort();

  /** Network address to bind to, optional */
  String getJmxHost();
}
