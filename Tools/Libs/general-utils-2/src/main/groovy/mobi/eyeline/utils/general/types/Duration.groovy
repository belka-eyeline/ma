package mobi.eyeline.utils.general.types

import com.google.common.base.MoreObjects
import groovy.transform.CompileStatic
import groovy.transform.Immutable

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import static java.lang.Long.parseLong
import static java.util.concurrent.TimeUnit.DAYS
import static java.util.concurrent.TimeUnit.HOURS
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.MINUTES
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@Immutable
final class Duration {

  private static final Pattern PATTERN = ~/^\s*(\d+)\s*([a-zA-Z]+)\s*$/

  long value
  TimeUnit unit

  long getValue(TimeUnit timeUnit)  { timeUnit.convert(value, unit) }
  long getMillis()                  { getValue MILLISECONDS }
  int  getSeconds()                 { getValue SECONDS }

  @Override
  boolean equals(Object obj) {
    obj instanceof Duration && (obj as Duration).millis == millis
  }

  static Duration duration(String duration) {
    final matcher = PATTERN.matcher duration
    
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid duration value: [$duration]")
    }

    new Duration(
        parseLong(matcher.group(1)),
        valueOfTimeUnit(matcher.group(2))
    )
  }

  private static TimeUnit valueOfTimeUnit(String timeUnitString) {
    switch (timeUnitString) {
      case ['ms', 'millis']:                    return MILLISECONDS
      case ['s', 'second', 'seconds']:          return SECONDS
      case ['m', 'min', 'minute', 'minutes']:   return MINUTES
      case ['h', 'hour', 'hours']:              return HOURS
      case ['d', 'day', 'days']:                return DAYS

      default:
        throw new IllegalArgumentException("Unknown time unit: [$timeUnitString]")
    }
  }

  @Override
  String toString() {
    MoreObjects
        .toStringHelper(this)
        .add('value', value)
        .add('unit', unit)
        .toString()
  }
}