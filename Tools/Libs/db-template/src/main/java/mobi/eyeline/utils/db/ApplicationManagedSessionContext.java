package mobi.eyeline.utils.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.util.HashMap;
import java.util.Map;

/**
 * Managed session context implementation allowing for manual specification of context boundaries.
 *
 * <p>Sample usage model is the following:
 * <pre>
 *   ContextDbServiceImpl db = ...
 *
 *   // (1) Start a context: session is opened and bound to the current thread.
 *   db.getContext().start();
 *
 *   // (2) Subsequent queries use a single session bound in (1).
 *   db.tx(...)
 *   db.query(...)
 *
 *   // (3) Close a context: session gets closed and unbound.
 *   db.getContext().close();
 *
 * </pre>
 *
 * @see ContextDBServiceImpl
 * @see ManagedSessionContext
 */
@SuppressWarnings("WeakerAccess")
public class ApplicationManagedSessionContext
    extends ManagedSessionContext
    implements CurrentSessionContext {

  private static final Map<SessionFactory, ApplicationManagedSessionContext> refs =
      new HashMap<SessionFactory, ApplicationManagedSessionContext>();

  private final SessionFactory sessionFactory;

  public ApplicationManagedSessionContext(SessionFactoryImplementor sessionFactory) {
    super(sessionFactory);
    this.sessionFactory = sessionFactory;

    refs.put(sessionFactory, this);
  }

  public void start() {
    start(sessionFactory.openSession());
  }

  /**
   * Starts execution context by binding a new session. It is mandatory that no session is
   * already present.
   *
   * @see #close()
   */
  public void start(Session s) {
    if (hasSession()) {
      throw new DBException("Stale session detected in the managed scope");
    }

    ManagedSessionContext.bind(s);
  }

  /**
   * Closes the current context by unbinding a session. It is mandatory that a session is already
   * present.
   *
   * @see #start()
   */
  public void close() {
    final Session unbound = unbind();
    if (unbound.isOpen()) {
      unbound.close();
    }
  }

  public Session unbind() {
    return ManagedSessionContext.unbind(sessionFactory);
  }

  /**
   * Returns {@code true} iff there's a session bound to the current context.
   */
  public boolean hasSession() {
    return hasBind(sessionFactory);
  }

  static ApplicationManagedSessionContext getContext(SessionFactory sessionFactory) {
    return refs.get(sessionFactory);
  }

}
