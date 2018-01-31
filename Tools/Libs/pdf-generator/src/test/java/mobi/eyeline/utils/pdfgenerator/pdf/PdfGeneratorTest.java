package mobi.eyeline.utils.pdfgenerator.pdf;

import mobi.eyeline.utils.pdfgenerator.pdf.PdfGenerator.DocumentGenerationException;
import mobi.eyeline.utils.pdfgenerator.templates.BaseTemplateService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static mobi.eyeline.utils.pdfgenerator.pdf.TestUtil.preview;

public class PdfGeneratorTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Test
  public void test() throws IOException, DocumentGenerationException {

    final MyTemplateGenerator templateGenerator = new MyTemplateGenerator();

    final String html = templateGenerator.formatMyDocument(new HashMap<String, Object>() {{
      put("date", "06.11.1989");
      put("docNumber", "120021");
      put("declarantName", "Николаев А. В.");
    }});

    final File pdfFile = tmp.newFile("pdf-sample.pdf");
    try (FileOutputStream out = new FileOutputStream(pdfFile)) {
      final byte[] pdfBytes = new PdfGenerator().htmlToPdf(html);
      out.write(pdfBytes);
    }

    preview(pdfFile);
  }


  //
  //
  //

  private static class MyTemplateGenerator extends BaseTemplateService {

    MyTemplateGenerator() {
      // Use classpath-located resources.
      init();
    }

    @Override
    protected List<String> getTemplateNames() {
      return singletonList(
          "/mobi/eyeline/utils/pdfgenerator/pdf/document-template.ftl"
      );
    }

    String formatMyDocument(Map<String, Object> data) {
      return processTemplate(
          "/mobi/eyeline/utils/pdfgenerator/pdf/document-template.ftl",
          data
      );
    }
  }

}
