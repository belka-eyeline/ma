package mobi.eyeline.utils.db;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import mobi.eyeline.utils.general.misc.Reiterator;
import mobi.eyeline.utils.general.misc.Reiterator.Action;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.Serializable;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class DBServiceImpl<Self extends DBServiceImpl<Self>> implements DBService {

  @SuppressWarnings("WeakerAccess")
  public static Reiterator RETRY_NEVER = new Reiterator(0);

  @SuppressWarnings("WeakerAccess")
  protected final SessionFactory sf;

  protected final ValidatorFactory validatorFactory;

  private Reiterator queryReiterator = RETRY_NEVER;

  public DBServiceImpl(Properties hibernateProperties, String... configResource) {
    sf = buildSessionFactory(hibernateProperties, configResource);
    validatorFactory = buildValidatorFactory();
  }

  protected DBServiceImpl(SessionFactory sessionFactory, ValidatorFactory validatorFactory) {
    this.sf = sessionFactory;
    this.validatorFactory = validatorFactory;
  }

  protected SessionFactory buildSessionFactory(Properties hibernateProperties,
                                               String... configResource) {

    final StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();

    for (String cfg : configResource) {
      builder.configure(cfg);
    }

    final StandardServiceRegistry registry = builder
        .applySettings(Maps.fromProperties(hibernateProperties))
        .build();

    try {
      return new MetadataSources(registry)
          .buildMetadata()
          .buildSessionFactory();

    } catch (Exception e) {
      StandardServiceRegistryBuilder.destroy(registry);
      throw Throwables.propagate(e);
    }
  }

  protected ValidatorFactory buildValidatorFactory() {
    return Validation.buildDefaultValidatorFactory();
  }

  /**
   * Specifies failed query reiteration strategy.
   * Default is to never repeat unsuccessful queries.
   *
   * @see #RETRY_NEVER
   */
  public Self setQueryReiterator(Reiterator queryReiterator) {
    this.queryReiterator = checkNotNull(queryReiterator);

    //noinspection unchecked
    return (Self) this;
  }

  @Override
  public <T> T tx(final Tx<T> tx) {
    try {
      return queryReiterator.call(new Action<T>() {
        @Override
        public T call() {
          T result;
          final Session s = openSession();
          try {
            s.beginTransaction();

            result = tx.tx(s);

            s.getTransaction().commit();

          } catch (Exception e) {
            s.getTransaction().rollback();
            throw Throwables.propagate(e);

          } finally {
            closeSession(s);
          }

          return result;
        }
      });

    } catch (Exception e) {
      Throwables.propagateIfInstanceOf(e, DBException.class);
      throw new DBException(e);
    }
  }

  @Override
  public <T> T query(final Tx<T> tx) {
    try {
      return queryReiterator.call(new Action<T>() {
        @Override
        public T call() {
          final Session s = openSession();
          final boolean defaultReadOnly = s.isDefaultReadOnly();
          try {
            s.setDefaultReadOnly(true);
            return tx.tx(s);

          } finally {
            if (s.isOpen()) {
              s.setDefaultReadOnly(defaultReadOnly);
            }
            closeSession(s);
          }
        }
      });

    } catch (Exception e) {
      Throwables.propagateIfInstanceOf(e, DBException.class);
      throw new DBException(e);
    }
  }

  @Override
  public void vtx(final VTx tx) {
    tx(new Tx<Void>() {
      @Override
      public Void tx(Session s) {
        tx.tx(s);
        return null;
      }
    });
  }

  @Override
  public <T> T stateless(final Stateless<T> tx)  {
    try {
      return queryReiterator.call(new Action<T>() {
        @Override
        public T call() {
          final StatelessSession s = openStatelessSession();
          try {
            return tx.run(s);

          } finally {
            s.close();
          }
        }
      });

    } catch (Exception e) {
      Throwables.propagateIfInstanceOf(e, DBException.class);
      throw new DBException(e);
    }
  }

  @Override
  public void vstateless(final VStateless tx)  {
    stateless(new Stateless<Void>() {
      @Override
      public Void run(StatelessSession s) {
        tx.run(s);
        return null;
      }
    });
  }

  @Override
  public <T> Set<ConstraintViolation<T>> validateObject(T obj) {
    return validatorFactory.getValidator().validate(obj);
  }

  @Override
  public boolean isValidObject(Object obj) {
    return validateObject(obj).isEmpty();
  }

  @Override
  public Self with(Reiterator reiterator) {
    return new DBServiceImpl<Self>(this.sf, this.validatorFactory)
        .setQueryReiterator(checkNotNull(reiterator));
  }

  public void destroy() {
    sf.close();
  }

  protected Session openSession() {
    return sf.openSession();
  }

  protected void closeSession(Session session) {
    session.close();
  }

  private StatelessSession openStatelessSession() {
    return sf.openStatelessSession();
  }

  protected EventListenerRegistry getEventRegistry() {
    return ((SessionFactoryImpl) sf).getServiceRegistry().getService(EventListenerRegistry.class);
  }

  /**
   * In case {@code obj} is a lazy Hibernate proxy, loads it eagerly.
   * Note: might fail if proxying mechanism changes.
   *
   * @return fully initialized object.
   */
  public static <T> T unwrap(T obj) {
    if (obj instanceof HibernateProxy) {
      final HibernateProxy proxy = (HibernateProxy) obj;
      //noinspection unchecked
      return (T) proxy.getHibernateLazyInitializer().getImplementation();

    } else {
      // XXX: What about lazy collections?
      Hibernate.initialize(obj);
      return obj;
    }
  }

  public static Serializable getEntityId(Object entity, Session session) {
    if (entity == null)
      return null;
    if (entity instanceof HibernateProxy) {
      LazyInitializer lazyInitializer = ((HibernateProxy) entity).getHibernateLazyInitializer();
      return lazyInitializer.getIdentifier();
    }
    return session.getIdentifier(entity);
  }

  public static Runnable getDestroyCallback(final DBService self) {
    return new Runnable() {
      @Override public void run() { self.destroy(); }
    };
  }
}
