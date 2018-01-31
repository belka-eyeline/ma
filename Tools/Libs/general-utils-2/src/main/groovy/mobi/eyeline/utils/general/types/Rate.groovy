package mobi.eyeline.utils.general.types;

import groovy.transform.Immutable

@Immutable
final class Rate {
  int rate
  Duration interval

  static Rate rateOf(int rate, Duration interval) {
    new Rate(rate, interval)
  }

  @Override
  String toString() {
    "Rate{rate=$rate,interval=$interval}"
  }
}