package mobi.eyeline.utils.pdfgenerator.pdf;

import mobi.eyeline.utils.pdfgenerator.pdf.PdfGenerator.DocumentGenerationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static mobi.eyeline.utils.pdfgenerator.pdf.TestUtil.preview;
import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;

/**
 * This demonstrates writing cyrillic text in specified coordinates of an existing PDF document.
 */
public class PdfWriteTest {

  private static final String SAMPLE_PDF = "/mobi/eyeline/utils/pdfgenerator/pdf/pdf-sample.pdf";
  private static final String SAMPLE_FONT = "/mobi/eyeline/utils/pdfgenerator/fonts/Arial.ttf";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void test() throws IOException, DocumentGenerationException {

    final File pdfFile = tmp.newFile("pdf-sample.pdf");

    try (PDDocument doc = PDDocument.load(getClass().getResourceAsStream(SAMPLE_PDF))) {

      final PDType0Font font = PDType0Font.load(
          doc,
          getClass().getResourceAsStream(SAMPLE_FONT));

      final PDPage page = doc.getPage(0);
      try (PDPageContentStream cs = new PDPageContentStream(doc, page, APPEND, true)) {
        cs.beginText();
        cs.setFont(font, 18);
        cs.newLineAtOffset(100, 300);
        cs.showText("Привет!!");
        cs.endText();
      }

      try (FileOutputStream out = new FileOutputStream(pdfFile)) {
        doc.save(out);
      }
    }

    preview(pdfFile);
  }

  /**
   * Demonstrates printing centered text.
   */
  @Test
  public void testCentered() throws IOException, DocumentGenerationException {

    final File pdfFile = tmp.newFile("pdf-sample.pdf");

    try (PDDocument doc = PDDocument.load(getClass().getResourceAsStream(SAMPLE_PDF))) {

      final PDType0Font font = PDType0Font.load(
          doc,
          getClass().getResourceAsStream(SAMPLE_FONT));

      final PDPage page = doc.getPage(0);
      try (PDPageContentStream cs = new PDPageContentStream(doc, page, APPEND, true)) {

        PdfUtil.addCenteredText(cs, page, "Привееет!", font, 36);

        // Shift down a bit.
        PdfUtil.addCenteredText(cs, page, "Привееет!", font, 36, new Point2D.Float(0, 200));
      }

      try (FileOutputStream out = new FileOutputStream(pdfFile)) {
        doc.save(out);
      }
    }

    preview(pdfFile);
  }

}
