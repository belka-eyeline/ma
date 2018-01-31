package mobi.eyeline.utils.db;

import mobi.eyeline.utils.general.misc.Reiterator;
import org.hibernate.Session;
import org.hibernate.StatelessSession;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * Methods might throw {@linkplain DBException}, and it is guaranteed
 * to be the only exception thrown.
 */
public interface DBService {

  /** Executes callback in a new session w/o any explicit transactions. */
  <T> T query(Tx<T> tx);

  /** Executes callback in transaction, started in a new session. */
  void vtx(VTx tx);

  /** Executes callback in transaction, started in a new session. */
  <T> T tx(Tx<T> tx);

  /** Executes callback in stateless session. */
  void vstateless(VStateless tx);

  /** Executes callback in stateless session. */
  <T> T stateless(Stateless<T> tx);

  <T> Set<ConstraintViolation<T>> validateObject(T obj);
  boolean isValidObject(Object obj);

  DBService with(Reiterator reiterator);

  interface Stateless<T>  { T run(StatelessSession s); }
  interface VStateless    { void run(StatelessSession s); }

  interface Tx<T>         { T tx(Session s); }
  interface VTx           { void tx(Session s); }

  void destroy();
}
