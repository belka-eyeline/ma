package mobi.eyeline.utils.mx.service;

import com.google.common.base.MoreObjects;
import com.j256.simplejmx.server.JmxServer;
import org.apache.log4j.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class JmxBeansService {

  private final Logger log = Logger.getLogger(JmxBeansService.class);

  private JmxServer jmxServer;

  public JmxBeansService(JmxConfig config) {

    if (!config.isJmxEnabled()) {
      log.info("JMX disabled");
      return;
    }

    log.info("Initializing JMX service");

    // This effectively uses the same port for RMI registry and RMI server.
    jmxServer = new JmxServer(config.getJmxPort());

    // Handle callbacks for NATs, host parameter is optional.
    if (config.getJmxHost() != null) {
      try {
        jmxServer.setInetAddress(Inet4Address.getByName(config.getJmxHost()));

      } catch (UnknownHostException e) {
        throw new RuntimeException("Invalid JMX host [" + config.getJmxHost() + "]", e);
      }
    }

    try {
      // Start own MBean server bypassing the one (possibly) created by JVM.
      jmxServer.start();

    } catch (JMException e) {
      log.error("JMX initialization failed", e);
    }

  }

  /**
   * Registers bean annotated w/ SimpleJmx qualifiers.
   * For standard beans see {@linkplain #registerStandardMBean(Object, ObjectName)}.
   */
  public void register(Object object) {
    try {
      if (jmxServer != null) {
        jmxServer.register(object);
      }

    } catch (JMException e) {
      log.error("Management bean registration failed", e);
    }
  }

  public void registerStandardMBean(Object mBean,
                                    ObjectName objectName) {

    try {
      if (jmxServer != null) {
        final MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        mbeanServer.registerMBean(mBean, objectName);
      }

    } catch (JMException e) {
      log.error("Management bean registration failed", e);
    }
  }

  public void stop() {
    if (jmxServer != null) {
      log.info("Stopping JMX service");
      jmxServer.stop();
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("enabled", jmxServer != null)
        .add("port", jmxServer == null ? null : jmxServer.getRegistryPort())
        .toString();
  }
}
