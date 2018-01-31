package mobi.eyeline.utils.general.misc

import com.google.common.base.MoreObjects
import com.google.common.base.Predicate
import com.google.common.base.Predicates
import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class PredicateHelper {

  static Predicate<String> isAllowed(Pattern whiteListPattern,
                                     Pattern blackListPattern) {

    if (!whiteListPattern && !blackListPattern) {
      // Fast path
      return Predicates.alwaysTrue()
    }

    final allowed =
        whiteListPattern ? matchesPattern(whiteListPattern) : Predicates.alwaysTrue() as Predicate<String>
    final rejected =
        blackListPattern ? matchesPattern(blackListPattern) : Predicates.alwaysFalse() as Predicate<String>

    Predicates.or allowed, Predicates.not(rejected)
  }


  //
  //
  //

  static Predicate<String> matchesPattern(Pattern pattern) {
    new Predicate<String>() {
      @Override boolean apply(String value) { value.matches(pattern) }

      @Override
      String toString() {
        MoreObjects
            .toStringHelper('MatchesPattern')
            .add('pattern', pattern)
            .toString()
      }
    }
  }

}
