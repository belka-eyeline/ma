package mobi.eyeline.utils.general.caches

import groovy.transform.CompileStatic
import mobi.eyeline.utils.general.types.Duration
import org.junit.Test

import static org.junit.Assert.assertEquals

@CompileStatic
class ExpiringCacheTest {

  @Test
  void test1() {
    final impl = new ExpiringCache<String>()
    impl.lifeTime = Duration.duration '1m'

    impl.get 'foo', {'bar'}

    impl.lifeTime = Duration.duration '2m'

    // Value is preserved.
    final prevValue = impl.get 'foo', { 'baz' }
    assertEquals 'bar', prevValue
  }

}
