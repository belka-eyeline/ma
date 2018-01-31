package mobi.eyeline.utils.dbconf;

public interface DbConfigChangedEventListener {

  void onConfigChanged(DbConfigChangedEvent event);

}
