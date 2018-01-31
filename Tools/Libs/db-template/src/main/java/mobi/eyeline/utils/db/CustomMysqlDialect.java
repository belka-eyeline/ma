package mobi.eyeline.utils.db;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class CustomMysqlDialect extends MySQL5InnoDBDialect {

  public CustomMysqlDialect() {
    super();

    registerKeyword("interval");
    registerKeyword("day");
  }

  @Override
  public String getTableTypeString() {
    // Force Hibernate to set UTF-8 table charset if automatically creating schema for the MySQL DB.
    // Need this for running automated tests.
    return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
  }
}