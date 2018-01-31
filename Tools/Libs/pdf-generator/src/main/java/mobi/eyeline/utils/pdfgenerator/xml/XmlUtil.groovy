package mobi.eyeline.utils.pdfgenerator.xml

import groovy.transform.CompileStatic
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import javax.xml.bind.JAXBElement
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

import static javax.xml.xpath.XPathConstants.NODESET

@CompileStatic
class XmlUtil {

  static Document parseXml(InputSource inputSource) throws IOException, SAXException {

    final dbf = DocumentBuilderFactory.newInstance()

    // Might need additional configuration in case input format is not so strict.

    // dbf.setValidating(false)
    // dbf.setNamespaceAware(true)
    // dbf.setIgnoringComments(false)
    // dbf.setIgnoringElementContentWhitespace(false)
    // dbf.setExpandEntityReferences(false)

    try {
      dbf.newDocumentBuilder().parse(inputSource)

    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e)
    }
  }

  static Document parseXml(InputStream xml) {
    parseXml new InputSource(xml)
  }

  static Document parseXml(String xml) {
    parseXml new InputSource(new StringReader(xml))
  }

  static void dump(Document doc, OutputStream target) {
    TransformerFactory.newInstance()
        .newTransformer()
        .transform new DOMSource(doc), new StreamResult(target)
  }

  static String dump(Document doc) {
    new ByteArrayOutputStream().with { _ ->
      dump doc, _
      _.toString 'UTF-8'
    }
  }

  /**
   * @return Transformer for the provided XSL.
   *
   * Note: the instance returned is reusable, but not thread-safe.
   */
  static Transformer createXslTransformer(InputStream xsl) {

    final transformerFactory = TransformerFactory.newInstance()

    final documentBuilderFactory = DocumentBuilderFactory.newInstance()
    documentBuilderFactory.namespaceAware = true

    final Document xslDoc = documentBuilderFactory
        .newDocumentBuilder()
        .parse(xsl)

    transformerFactory.newTransformer new DOMSource(xslDoc)
  }

  static Document transform(Document source, Transformer transformer) {
    final xmlDomSource = new DOMSource(source)
    final domResult = new DOMResult()

    transformer.transform xmlDomSource, domResult

    domResult.getNode() as Document
  }

  private static void removeNodes(Node node, short nodeType, String name = null) {
    final childNodes = getChildNodes node

    childNodes
        .each { child -> removeNodes child, nodeType, name }

    if ((node.nodeType == nodeType) &&
        ((name == null) || node.getNodeName().equals(name))) {
      node.getParentNode().removeChild(node)
    }
  }

  /**
   * For each node descending from the specified one (including itself) replaces attribute values.
   *
   * @param replacements Maps attribute name to the expected value.
   */
  static void replaceAttributeValues(Node node, Map<String, String> replacements) {
    final childNodes = getChildNodes node

    childNodes
        .each { child -> replaceAttributeValues child, replacements }

    final attributes = node.getAttributes()
    if (attributes == null) {
      return
    }

    replacements.each { attrName, expectedValue ->
      final attr = attributes.getNamedItem attrName
      if (attr != null && Objects.equals(expectedValue, attr.getNodeValue())) {
        attr.nodeValue = expectedValue
      }
    }
  }

  private static List<Node> getChildNodes(Node node) {
    (0..<node.childNodes.length).collect { _ -> node.childNodes.item _ }
  }

  /**
   * Removes all comment nodes from the specified document, modifications are performed inline.
   *
   * @return the specified document (for chaining).
   */
  private static Document stripComments(Document doc) {
    removeNodes doc, Node.COMMENT_NODE
    doc
  }

  /**
   * Removes all empty text nodes, modifications are performed inline.
   *
   * @return the specified document (for chaining).
   */
  private static Document stripText(Document doc) {
    final XPath xPath = XPathFactory.newInstance().newXPath()

    try {
      while (true) {
        final emptyTextNodes =
            xPath.evaluate("//text()[normalize-space(.)='']", doc, NODESET) as NodeList

        final nRemoved = emptyTextNodes.length

        if (nRemoved == 0) return doc
        else (0..<nRemoved)
            .each { i -> emptyTextNodes.item(i).parentNode.removeChild emptyTextNodes.item(i) }
      }

    } catch (XPathExpressionException e) {
      throw new RuntimeException(e)
    }
  }

  /**
   * Removes empty text nodes and comments from the specified document.
   *
   * @return the specified document (for chaining).
   */
  static Document strip(Document doc) {
    stripText stripComments(doc)
  }

  static <T> T deref(JAXBElement<T> ref) {
    (ref == null) ? null : ref.value
  }
}
