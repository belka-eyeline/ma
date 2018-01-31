package mobi.eyeline.utils.general.misc;

public class NameUtils {

  /**
   * Returns the full name ("LastName FirstName MiddleName").
   */
  public static String getFullName(String lastName,
                                   String firstName,
                                   String middleName) {

    final StringBuilder buf = new StringBuilder();

    assert lastName != null && !lastName.isEmpty();
    buf.append(lastName);

    assert firstName != null && !firstName.isEmpty();
    buf.append(" ").append(firstName);

    if (middleName != null && !middleName.isEmpty()) {
      buf.append(" ").append(middleName);
    }

    return buf.toString();
  }

  /**
   * Abbreviates the name.
   *
   * For instance, "LastName FirstName MiddleName" -> "LastName F. M."
   */
  public static String getAbbreviatedName(String lastName,
                                          String firstName,
                                          String middleName) {

    final StringBuilder buf = new StringBuilder();

    assert lastName != null && !lastName.isEmpty();
    buf.append(lastName);

    assert firstName != null && !firstName.isEmpty();
    buf.append(" ").append(firstName.charAt(0)).append(".");

    if (middleName != null && !middleName.isEmpty()) {
      buf.append(" ").append(middleName.charAt(0)).append(".");
    }

    return buf.toString();
  }

}
