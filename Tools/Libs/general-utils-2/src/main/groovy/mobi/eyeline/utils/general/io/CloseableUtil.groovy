package mobi.eyeline.utils.general.io

import groovy.transform.CompileStatic

@CompileStatic
class CloseableUtil {

  static <T> T closeIfPossible(T obj) {
    if (obj instanceof Closeable) {
      ((Closeable) obj).close()
    }

    obj
  }

}
