package mobi.eyeline.utils.db;

import org.hibernate.Session;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkState;

/**
 * Implements session caching for an execution context.
 *
 * <p>The session used for the queries first is tried to be obtained from the context,
 * but falls back to a new instance. In case a new session is opened, it is not cached and
 * gets closed on query finish.
 *
 * <p>This logic doesn't apply to {@linkplain org.hibernate.StatelessSession stateless} sessions,
 * which are never cached.
 *
 * <p>Context boundaries should be defined manually (see {@linkplain #getContext()}.
 *
 * Hibernate configuration must specify respective session context implementation as follows:
 * <pre>
 *   &lt;property name="db.hibernate.current_session_context_class"
 *             value="mobi.eyeline.utils.db.ApplicationManagedSessionContext"/&gt;
 * </pre>
 */
public class ContextDBServiceImpl extends DBServiceImpl<ContextDBServiceImpl> {

  public ContextDBServiceImpl(Properties hibernateProperties,
                              String... configResource) {

    super(hibernateProperties, configResource);
  }

  public ApplicationManagedSessionContext getContext() {
    final ApplicationManagedSessionContext context =
        ApplicationManagedSessionContext.getContext(sf);

    checkState(context != null, "Managed session context is not set.");
    return context;
  }

  @Override
  protected Session openSession() {
    if (getContext().hasSession()) {
      return sf.getCurrentSession();

    } else {
      return sf.openSession();
    }
  }

  @Override
  protected void closeSession(Session session) {
    if (!getContext().hasSession()) {
      session.close();
    }
  }
}
