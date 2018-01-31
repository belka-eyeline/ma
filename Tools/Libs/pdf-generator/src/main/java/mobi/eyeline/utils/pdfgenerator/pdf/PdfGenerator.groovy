package mobi.eyeline.utils.pdfgenerator.pdf

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Log4j
import mobi.eyeline.utils.pdfgenerator.xml.XmlUtil
import org.apache.avalon.framework.configuration.ConfigurationException
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder
import org.apache.commons.io.IOUtils
import org.apache.fop.apps.FOURIResolver
import org.apache.fop.apps.FopFactory
import org.apache.fop.apps.FopFactoryConfigurator
import org.apache.fop.apps.MimeConstants
import org.fit.cssbox.css.DOMAnalyzer
import org.w3c.dom.Document
import org.xml.sax.SAXException

import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.URIResolver
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.stream.StreamSource

import static mobi.eyeline.utils.pdfgenerator.xml.XmlUtil.createXslTransformer

/**
 * Not thread-safe.
 */
@CompileStatic
@Log4j
class PdfGenerator {

  private static final String RESOURCE_FOP_CONFIG = '/mobi/eyeline/utils/pdfgenerator/fop/fop.xml'
  private static final String RESOURCE_XHTML_XSLT = '/mobi/eyeline/utils/pdfgenerator/fop/xhtml2fo.xsl'

  @SuppressWarnings('GrFinalVariableAccess')
  private final FopFactory fopFactory

  @SuppressWarnings('GrFinalVariableAccess')
  private final Transformer html2FoTransformer

  PdfGenerator() {
    try {
      fopFactory          = createFopFactory getClass().getResourceAsStream(RESOURCE_FOP_CONFIG)
      html2FoTransformer  = createXslTransformer getClass().getResourceAsStream(RESOURCE_XHTML_XSLT)

      html2FoTransformer.setParameter 'page-number-print-in-footer', 'false'

    } catch (e) {
      throw new RuntimeException(e)
    }
  }

  byte[] htmlToPdf(String html) throws DocumentGenerationException {
    try {
      new ByteArrayOutputStream().with { _ ->
        htmlToPdf IOUtils.toInputStream(html, 'UTF-8'), _
        _.toByteArray()
      }

    } catch (IOException e) {
      throw new DocumentGenerationException(e)
    }
  }

  void htmlToPdf(InputStream html, OutputStream pdf)
      throws DocumentGenerationException {

    log.trace 'Converting HTML -> PDF'

    try {
      final xmlDoc = XmlUtil.parseXml html

      // As HTML -> FO transformation doesn't support CSS selectors (but fortunately understands
      // inline style attributes), propagate embedded `<style>' tags declarations
      // to corresponding elements.
      inlineStyles(xmlDoc)

      log.trace "Parsed HTML: [${XmlUtil.dump(xmlDoc)}]"

      // Converts XHTML document to FO using XSLT transformation.
      final foDoc = XmlUtil.transform(xmlDoc, html2FoTransformer)

      log.trace "Generated FO: [${XmlUtil.dump(foDoc)}]"

      fo2PDF foDoc, pdf

    } catch (e) {
      throw new DocumentGenerationException(e)
    }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  private void inlineStyles(Document doc) {
    final domAnalyzer = new DOMAnalyzer(doc)
    domAnalyzer.attributesToStyles()
    domAnalyzer.getStyleSheets()
    domAnalyzer.stylesToDomInherited()

    // Remove `<style>' tags as already inlined and unsupported by HTML->FO transformation.
    final styleTags = doc.getElementsByTagName('style')
    (0..<styleTags.length).each { int i ->
      styleTags.item(i).parentNode.removeChild styleTags.item(i)
    }
  }

  private void foSource2PDF(Source src,
                            OutputStream target) throws SAXException, TransformerException {

    final fop = fopFactory.newFop MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), target

    // Setup input and output for XSLT transformation.
    final dst = new SAXResult(fop.getDefaultHandler())

    // Use identity transformer here as resulting SAX events for the generated FO
    // will anyway be piped through FOP handler.
    final idTransformer = TransformerFactory.newInstance().newTransformer()

    // Start XSLT transformation and FOP processing
    idTransformer.transform src, dst
  }

  private void fo2PDF(Document foDocument,
                      OutputStream target) throws SAXException, TransformerException {

    foSource2PDF new DOMSource(foDocument), target
  }

  @SuppressWarnings('GroovyUnusedDeclaration')
  private void fo2PDF(InputStream fo,
                      OutputStream target) throws SAXException, TransformerException {
    foSource2PDF new StreamSource(fo), target
  }

  /**
   * @param xmlConfig Global FOP XML configuration, ignored if {@code null}.
   */
  @SuppressWarnings('GrMethodMayBeStatic')
  private FopFactory createFopFactory(InputStream xmlConfig)
      throws SAXException, IOException, ConfigurationException {

    final cfg = new DefaultConfigurationBuilder().build(xmlConfig)

    final fopFactory = FopFactory.newInstance()
    if (cfg != null) {
      // Set ref in the factory for future use (e.g. by output renderer instances).
      fopFactory.setUserConfig cfg

      // Parse and set global factory options.
      new FopFactoryConfigurator(fopFactory).setUserConfig cfg
    }

    // By default, all paths are are resolved against the filesystem.
    final uriResolver = (FOURIResolver) fopFactory.getURIResolver()
    uriResolver.customURIResolver = new ClasspathUriResolver()

    fopFactory
  }

  /**
   * Resolves URIs as classpath resources ignoring base path.
   */
  private static class ClasspathUriResolver implements URIResolver {
    @Override
    Source resolve(String href, String base) throws TransformerException {
      final resource = getClass().getResourceAsStream(href)
      (resource != null) ? new StreamSource(resource) : null
    }
  }

  @InheritConstructors
  static class DocumentGenerationException extends Exception {}
}
