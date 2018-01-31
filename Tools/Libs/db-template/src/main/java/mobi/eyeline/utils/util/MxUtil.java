package mobi.eyeline.utils.util;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PoolBackedDataSource;
import com.mchange.v2.c3p0.WrapperConnectionPoolDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.management.MBeanServer;
import javax.management.MXBean;
import javax.management.ObjectName;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Set;

public class MxUtil {

  /**
   * Registers Hibernate {@linkplain Statistics statistics} data via management bean.
   *
   * @see StatisticsMXBean The interface exposed.
   */
  public static void registerHibernateStatsBean(SessionFactory sf,
                                                MBeanServer mbeanServer) throws Exception {

    final ObjectName statsName = ObjectName.getInstance("org.hibernate:type=statistics");

    final Statistics statistics = sf.getStatistics();
    statistics.setStatisticsEnabled(true);

    final InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(statistics, args);
      }
    };

    final Object statisticsMBean = Proxy.newProxyInstance(
        MxUtil.class.getClassLoader(),
        new Class<?>[]{StatisticsMXBean.class},
        handler);

    if (!mbeanServer.isRegistered(statsName)) {
      mbeanServer.registerMBean(statisticsMBean, statsName);
    }
  }

  /**
   * Registers C3P0 data source details.
   *
   * @see DataSource The interface exposed.
   */
  public static void registerDataSourceBean(MBeanServer mbeanServer) throws Exception {
    final ObjectName statsName = ObjectName.getInstance("com.mchange.v2.c3p0:type=DataSourceMBean");
    if (!mbeanServer.isRegistered(statsName)) {
      mbeanServer.registerMBean(
          getSingleDataSource(),
          statsName
      );
    }
  }

  private static DataSource getSingleDataSource() throws Exception {
    final Set<?> dataSources = C3P0Registry.allPooledDataSources();

    if (dataSources.size() != 1) {
      throw new Exception("Expected a single data source, got [" + dataSources + "]");
    }

    final PoolBackedDataSource dataSource = (PoolBackedDataSource) dataSources.iterator().next();
    return new DataSource(dataSource);
  }

  public static class DataSource implements DataSourceMBean {
    private final PoolBackedDataSource dataSource;

    public DataSource(PoolBackedDataSource dataSource) {
      this.dataSource = dataSource;
    }

    @Override
    public int getNumBusyConnections() throws SQLException {
      return dataSource.getNumBusyConnectionsDefaultUser();
    }

    @Override
    public int getNumIdleConnections() throws SQLException {
      return dataSource.getNumIdleConnectionsDefaultUser();
    }

    @Override
    public int getMaxPoolSize() {
      final WrapperConnectionPoolDataSource poolDataSource =
          (WrapperConnectionPoolDataSource) dataSource.getConnectionPoolDataSource();
      return poolDataSource.getMaxPoolSize();
    }

    @Override
    public int getRemainingConnections() throws SQLException {
      return getMaxPoolSize() - getNumBusyConnections();
    }
  }

  @MXBean
  public interface StatisticsMXBean extends Statistics {}

  public interface DataSourceMBean extends Serializable {
    int getNumBusyConnections() throws SQLException;
    int getNumIdleConnections() throws SQLException;
    int getMaxPoolSize();
    int getRemainingConnections() throws SQLException;
  }
}
