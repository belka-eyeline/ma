package mobi.eyeline.utils.pdfgenerator.pdf

import java.awt.*

class TestUtil {

  private static boolean doPreview = false

  /**
   * For manual runs: see we've generated.
   */
  static void preview(File pdfFile) {
    if (doPreview) {
      Desktop.getDesktop().open pdfFile

      // Give viewer some time to open the file before temporary data cleanup.
      Thread.sleep 500
    }
  }

}
