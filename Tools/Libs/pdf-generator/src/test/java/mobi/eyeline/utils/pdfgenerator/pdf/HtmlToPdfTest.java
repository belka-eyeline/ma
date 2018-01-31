package mobi.eyeline.utils.pdfgenerator.pdf;

import mobi.eyeline.utils.pdfgenerator.util.ResourcePool;
import mobi.eyeline.utils.pdfgenerator.util.ResourcePool.ResourceVoidCallable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static mobi.eyeline.utils.pdfgenerator.pdf.TestUtil.preview;

public class HtmlToPdfTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void testHtmlToPdf() throws IOException, PdfGenerator.DocumentGenerationException {
    final InputStream htmlSample = getClass().getResourceAsStream("pdf-sample.html");

    final File pdfFile = tmp.newFile("pdf-sample.pdf");
    try (FileOutputStream out = new FileOutputStream(pdfFile)) {
      new PdfGenerator().htmlToPdf(htmlSample, out);
    }

    preview(pdfFile);
  }

  @Test
  public void testResourcePool() throws Exception {
    final InputStream htmlSample = getClass().getResourceAsStream("pdf-sample.html");

    // In case our resource is not thread safe (as it actually is with PdfGenerator),
    // using resource pool might prove handy.
    final ResourcePool<PdfGenerator> pool =
        new ResourcePool<PdfGenerator>(Runtime.getRuntime().availableProcessors()) {

      @Override protected PdfGenerator init() throws Exception { return new PdfGenerator(); }

    };

    final File file = tmp.newFile("pdf-sample.pdf");
    try (FileOutputStream out = new FileOutputStream(file)) {
      pool.run(new ResourceVoidCallable<PdfGenerator>() {
        @Override
        public void call(PdfGenerator resource) throws Exception {
          resource.htmlToPdf(htmlSample, out);
        }
      });
    }
  }

}
