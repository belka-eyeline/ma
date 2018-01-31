package mobi.eyeline.utils.general.types;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.google.common.base.Throwables.getCausalChain;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.transform;

public class TypeUtil {

  public static <X> Class<X> getGenericType(Class<?> self, int argNo) {
    Type generic = self.getGenericSuperclass();
    if (generic instanceof Class) {
      generic = ((Class) generic).getGenericSuperclass();
    }

    final ParameterizedType genericSuperclass =
        (ParameterizedType) generic;
    //noinspection unchecked
    return (Class<X>) genericSuperclass.getActualTypeArguments()[argNo];
  }

  public static <T> Function<T, Class<?>> getClazz() {
    return new Function<T, Class<?>>() {
      @Override public Class<?> apply(T _) { return _.getClass(); }
    };
  }

  @SuppressWarnings("unchecked")
  public static <F, T extends F> Function<F, T> cast() {
    return new Function<F, T>() {
      @Override public T apply(F _) { return (T) _; }
    };
  }

  public static <T> Predicate<T> hasClass(final Class<? extends T> clazz) {
    return new Predicate<T>() {
      @Override public boolean apply(T _) { return getClazz().apply(_) == clazz; }
    };
  }

  public static boolean chainContains(Exception e, Class<? extends Exception> type) {
    //noinspection ThrowableResultOfMethodCallIgnored
    return getFirstInChain(e, type) != null;
  }

  public static <T extends Throwable> T getFirstInChain(Throwable e, Class<T> type) {
    return getFirst(
        transform(
            filter(getCausalChain(e), TypeUtil.<Throwable>hasClass(type)),
            TypeUtil.<Throwable, T>cast()
        ),
        null
    );
  }

}
