import java.util.ArrayList;
import java.util.List;

public class Main {

  public static final List<Object> target = new ArrayList<Object>();

  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      try {
        final byte[] _1Gb = new byte[1024 * 1024 * 1024];
        target.add(_1Gb);

      } catch (Throwable e) {
        System.out.println("Attempt #" + i + ": " + e.getMessage());
      }
    }

  }
}
