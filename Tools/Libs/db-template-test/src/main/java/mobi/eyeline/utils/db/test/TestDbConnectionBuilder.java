package mobi.eyeline.utils.db.test;

import com.mysql.management.MysqldResourceI;
import org.apache.log4j.Logger;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <h2>Example: connecting to an external DB instance, updating schema if necessary</h2>
 * <pre>
 *
 *   final connectionDetails =
 *       new TestDbConnectionBuilder()
 *           .setCredentials('tester', 'tester')
 *           .setDbName('test_db')
 *           .setConnectionUrl('jdbc:mysql://localhost/')
 *           .connect(
 *             ifNotInitialized(new CreateDatabaseIfNotExists()),
 *             ifNotInitialized(new InitializeSchema(getClass().getResource('/changes.xml'))),
 *             new CleanData()
 *       )
 * </pre>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class TestDbConnectionBuilder {

  private final Logger logger = Logger.getLogger(TestDbConnectionBuilder.class);

  private String username;
  private String password;

  private String dbName;
  private String connectionUrl;

  private int port = 3333;

  private long timeoutMillis = TimeUnit.MINUTES.toMillis(5);

  public TestDbConnectionBuilder() {}

  public TestDbConnectionBuilder setCredentials(String username, String password) {
    this.username = username;
    this.password = password;
    return this;
  }

  public TestDbConnectionBuilder setDbName(String dbName) {
    this.dbName = dbName;
    return this;
  }

  public TestDbConnectionBuilder setPort(int port) {
    this.port = port;
    return this;
  }

  public TestDbConnectionBuilder setConnectionUrl(String connectionUrl) {
    this.connectionUrl = connectionUrl;
    return this;
  }

  public TestDbConnectionBuilder setTimeout(long timeout, TimeUnit unit) {
    this.timeoutMillis = unit.toMillis(timeout);
    return this;
  }

  /**
   * Initialize database connection using embedded MySQL server, starting it if necessary.
   */
  public DbConnectionDetails initEmbedded(DbInitializer... initializers) throws Exception {
    logger.debug("Initializing embedded DB connection...");

    MysqlServiceManager.ensureStarted(
        new HashMap<String, String>() {{
          put("port", String.valueOf(port));
          put(MysqldResourceI.INITIALIZE_USER, "true");
          put(MysqldResourceI.INITIALIZE_USER_NAME, username);
          put(MysqldResourceI.INITIALIZE_PASSWORD, password);
        }},

        timeoutMillis
    );

    final DbConnectionDetails connectionDetails = new DbConnectionDetails();

    for (DbInitializer initializer : initializers) {
      connectionDetails.run(initializer);
    }

    logger.debug("Connection successfully initialized: [" + connectionDetails + "]");
    initializedDatabases.add(dbName);

    return connectionDetails;
  }

  /**
   * Connect to an existing MySQL instance.
   */
  public DbConnectionDetails connect(DbInitializer... initializers) throws Exception {
    logger.debug("Initializing standalone DB connection...");
    final DbConnectionDetails connectionDetails = new DbConnectionDetails();

    for (DbInitializer initializer : initializers) {
      connectionDetails.run(initializer);
    }

    logger.debug("Connection successfully initialized: [" + connectionDetails + "]");
    initializedDatabases.add(dbName);

    return connectionDetails;
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Test database connection details.
   */
  public class DbConnectionDetails {

    /**
     * DB Server location, e.g. {@literal jdbc:mysql://localhost:3333/}
     */
    public String getConnectionUrl() {
      return connectionUrl == null ? "jdbc:mysql://localhost:" + port + "/" : connectionUrl;
    }

    /**
     * Schema name, e.g. {@literal test_db}
     */
    public String getDbName() {
      return dbName;
    }

    /**
     * DB connection URL, e.g. {@literal jdbc:mysql://localhost:3333/test_db}
     */
    public String getDbConnectionUrl() {
      final String connectionParams =
          "mysql".equals(getDbType()) ?
              "?useUnicode=true&characterEncoding=UTF-8" +
                  "&allowMultiQueries=true&rewriteBatchedStatements=true" :
              "";

      String connectionUrl = getConnectionUrl();
      connectionUrl = connectionUrl.lastIndexOf("/") > 0 ?
          connectionUrl.substring(0, connectionUrl.lastIndexOf("/")) : connectionUrl;
      connectionUrl = connectionUrl.endsWith("/") ? connectionUrl : connectionUrl + "/";

      return connectionUrl + getDbName() + connectionParams;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

    /**
     * Hibernate-specific connection properties.
     */
    public Properties getHibernateProperties() {
      final Properties properties = new Properties();

      if ("mysql".equals(getDbType())) {
        properties.setProperty("hibernate.dialect", "mobi.eyeline.utils.db.CustomMysqlDialect");
      }

      properties.setProperty("hibernate.connection.url", getDbConnectionUrl());
      properties.setProperty("hibernate.connection.username", getUsername());
      properties.setProperty("hibernate.connection.password", getPassword());

      return properties;
    }

    void run(DbInitializer initializer) throws Exception {
      if (logger.isDebugEnabled()) {
        logger.debug("Calling connection initializer: [" + initializer + "]");
      }

      initializer.run(this);
    }

    private void doWork0(String connectionUrl, Work work) throws SQLException {
      final Connection conn = DriverManager.getConnection(
          connectionUrl,
          getUsername(),
          getPassword());

      try {
        work.execute(conn);

      } finally {
        conn.close();
      }
    }

    public void doWork(Work work) throws SQLException {
      doWork0(getConnectionUrl(), work);
    }

    public void doWorkInSchema(Work work) throws SQLException {
      doWork0(getDbConnectionUrl(), work);
    }

    private String getDbType() {
      return getConnectionUrl()
          .substring(
              "jdbc:".length(),
              getConnectionUrl().indexOf(':', "jdbc:".length())
          );
    }

    @Override
    public String toString() {
      return "DbConnectionDetails{" +
          "url='" + getDbConnectionUrl() + '\'' +
          ", username='" + getUsername() + "\'" +
          ", password=" + getPassword() + "\'" +
          '}';
    }
  }


  /**
   * Performs initialization of test database connection, e.g. schema creation, data cleanup etc.
   */
  public interface DbInitializer {
    void run(DbConnectionDetails connection) throws Exception;
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////
  //                                                                                              //
  //  Schema initialization routines.                                                             //
  //                                                                                              //
  //////////////////////////////////////////////////////////////////////////////////////////////////

  static Set<String> initializedDatabases = new HashSet<String>();

  static boolean isInitialized(DbConnectionDetails _) {
    return initializedDatabases.contains(_.getDbName());
  }

}
