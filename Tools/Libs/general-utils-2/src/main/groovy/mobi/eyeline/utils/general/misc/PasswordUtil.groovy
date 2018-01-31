package mobi.eyeline.utils.general.misc

import com.google.common.base.Preconditions
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.apache.commons.lang3.RandomUtils

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.xml.bind.DatatypeConverter
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

@CompileStatic
class PasswordUtil {

  static final String PBKDF2_ALGORITHM = 'PBKDF2WithHmacSHA256'

  /**
   * Hashes the password in the following format: {@literal "algorithm:iterations:hashSize:salt:hash"}.
   */
  static String hashPassword(String password,
                             int saltBytes = 24,
                             int hashBytes = 18,
                             int iterations = 64_000) {

    final salt = RandomUtils.nextBytes saltBytes

    new HashedPassword(
        'sha256',
        iterations,
        salt,
        pbkdf2(password, salt, iterations, hashBytes)
    ).toString()
  }

  /**
   * @throws java.lang.IllegalArgumentException In case {@code expectedHash} is not in the supported format.
   */
  static boolean verifyPassword(String password,
                                String expectedHash) throws IllegalArgumentException {

    Preconditions.checkNotNull password
    Preconditions.checkNotNull expectedHash

    final correct = HashedPassword.parse(expectedHash)
    Arrays.equals \
      correct.hash,
      pbkdf2(password, correct.salt, correct.iterations, correct.hash.length)
  }

  /**
   * Platform-provided implementation of <a href="https://en.wikipedia.org/wiki/PBKDF2">PBKDF2</a>.
   */
  private static byte[] pbkdf2(String password, byte[] salt, int iterations, int bytes) {
    try {
      final spec = new PBEKeySpec(password.toCharArray(), salt, iterations, bytes * 8)
      final skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
      return skf.generateSecret(spec).getEncoded()

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException('Hash algorithm not supported', e)

    } catch (InvalidKeySpecException e) {
      throw new RuntimeException('Invalid key spec', e)
    }
  }


  //
  //
  //

  @Immutable
  static class HashedPassword {
    String alg
    int iterations
    byte[] salt
    byte[] hash

    @Override
    String toString() {
      "$alg:$iterations:${hash.length}:${DatatypeConverter.printBase64Binary(salt)}:${DatatypeConverter.printBase64Binary(hash)}"
    }

    static HashedPassword parse(String formattedHash)
        throws IllegalArgumentException {

      try {
        final parts = formattedHash.split ':'
        if (parts[0] != 'sha256') {
          throw new IllegalArgumentException('Unsupported hash type')
        }

        final hashSize = parts[2].toInteger()
        final hash = DatatypeConverter.parseBase64Binary(parts[4])
        if (hashSize != hash.length) {
          throw new IllegalArgumentException('Invalid hash')
        }

        new HashedPassword(parts[0], parts[1].toInteger(), DatatypeConverter.parseBase64Binary(parts[3]), hash)

      } catch (Exception e) {
        throw new IllegalArgumentException('Invalid formatted hash value provided', e)
      }
    }
  }

}
