package mobi.eyeline.utils.util;

import mobi.eyeline.utils.db.CleanupDaemon;
import mobi.eyeline.utils.db.DBService;
import mobi.eyeline.utils.db.DBServiceImpl;

import static mobi.eyeline.utils.db.CleanupDaemon.registerCleanup;

/**
 * <p>
 *   Base resource class featuring database connection support.
 * </p>
 *
 * @see com.eyelinecom.whoisd.sads2.wstorage.resource.DbProfileStorage Usage sample as Mobilizer resource.
 *
 * @param <Repository>
 */
@SuppressWarnings("JavadocReference")
public class RepositoryResource<Repository, DB extends DBService> {

  protected final DB db;
  protected final Repository repository;

  private boolean deferDestruction;

  protected RepositoryResource(DB db, Repository repository) {
    CleanupDaemon.ensureInitialized();

    this.db = db;
    this.repository = repository;
  }

  @SuppressWarnings("WeakerAccess")
  public void destroy() {
    // Schedule DB implementation for cleanup once the storage becomes unreachable.
    // This avoids invalidation of the storage references still in use, but makes
    // an actual cleanup moment indeterminate.
    if (deferDestruction)  registerCleanup(repository, DBServiceImpl.getDestroyCallback(db));
    else                   db.destroy();
  }

  public void setDeferDestruction(boolean deferDestruction) {
    this.deferDestruction = deferDestruction;
  }

}
