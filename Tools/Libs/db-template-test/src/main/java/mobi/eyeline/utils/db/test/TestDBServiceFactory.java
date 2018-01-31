package mobi.eyeline.utils.db.test;

import mobi.eyeline.utils.db.DBService;
import mobi.eyeline.utils.db.DBServiceImpl;
import mobi.eyeline.utils.db.test.Initializers.InitializeSchema;
import mobi.eyeline.utils.db.test.MySqlInitializers.CreateDatabaseIfNotExists;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.Properties;

// TODO-1: try starting MySQL using other ports if the default one is taken.
// TODO-2: support manual schema initialization instead of hard-wired Liquibase support.
public class TestDBServiceFactory {

  private static Logger log = Logger.getLogger(TestDBServiceFactory.class);

  private static final int    DB_PORT        = 3333;
  private static final String DB_URL_BASE    = "jdbc:mysql://localhost:" + DB_PORT + "/";

  @SuppressWarnings("WeakerAccess")
  public static String DB_PROPERTIES         = "/test_db.properties";

  @SuppressWarnings("WeakerAccess")
  public static URL LIQUIBASE_CHANGESET      = TestDBServiceFactory.class.getResource("/changes.xml");

  public synchronized static DBService initEmbedded(final String dbName,
                                                    String hibernateConfig) throws Exception {
    final String dbUrl = DB_URL_BASE + dbName;

    final Properties properties = new Properties() {{
      load(TestDBServiceFactory.class.getResourceAsStream(DB_PROPERTIES));
      setProperty("hibernate.connection.url",
          dbUrl + "?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8");
    }};

    final String username = properties.getProperty("hibernate.connection.username");
    final String password = properties.getProperty("hibernate.connection.password", "");

    new TestDbConnectionBuilder()
        .setCredentials(username, password)
        .setDbName(dbName)
        .setPort(DB_PORT)
        .initEmbedded(new CreateDatabaseIfNotExists(), new InitializeSchema(LIQUIBASE_CHANGESET));

    return new DBServiceImpl(properties, hibernateConfig);
  }

  public synchronized static DBService connect(final String dbUrl,
                                               final String username,
                                               final String password,
                                               String hibernateConfig) throws Exception {

    final String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1, dbUrl.length());

    new TestDbConnectionBuilder()
        .setCredentials(username, password)
        .setDbName(dbName)
        .setPort(DB_PORT)
        .connect(new CreateDatabaseIfNotExists(), new InitializeSchema(LIQUIBASE_CHANGESET));

    final Properties properties = new Properties() {{
      load(TestDBServiceFactory.class.getResourceAsStream(DB_PROPERTIES));
      setProperty("hibernate.connection.url", dbUrl);
      setProperty("hibernate.connection.username", username);
      setProperty("hibernate.connection.password", password);
    }};

    return new DBServiceImpl(properties, hibernateConfig);
  }

  public synchronized static DBService connectReal(final String dbUrl,
                                                   final String username,
                                                   final String password,
                                                   String hibernateConfig) throws Exception {

    final String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1, dbUrl.length());

    new TestDbConnectionBuilder()
        .setCredentials(username, password)
        .setDbName(dbName)
        .setPort(DB_PORT)
        .connect();

    final Properties properties = new Properties() {{
      load(TestDBServiceFactory.class.getResourceAsStream(DB_PROPERTIES));
      setProperty("hibernate.connection.url", dbUrl);
      setProperty("hibernate.connection.username", username);
      setProperty("hibernate.connection.password", password);
    }};

    return new DBServiceImpl(properties, hibernateConfig);
  }

}
