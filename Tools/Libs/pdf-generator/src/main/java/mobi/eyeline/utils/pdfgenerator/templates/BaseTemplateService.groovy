package mobi.eyeline.utils.pdfgenerator.templates

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings('GrMethodMayBeStatic')
abstract class BaseTemplateService {

  // Using the same encoding for templates and generated content.
  private static final String ENCODING = 'UTF-8'

  protected Configuration configuration

  BaseTemplateService() {
  }

  /**
   * @param templatePath FS location of all the templates loaded by an implementation.
   */
  protected void init(String templatePath = null) {
    configuration =
        templatePath ? buildConfiguration(templatePath) : buildClasspathConfiguration()

    // Loaded templates are actually cached in the configuration, so reloading
    // doesn't bring in much overhead.
    preload()
  }

  protected List<String> preload() { templateNames.each { template it } }

  private Configuration buildConfiguration(String templatePath) {
    final path = new File(templatePath)
    if (!path.exists() || !path.isDirectory()) {
      throw new IllegalArgumentException("Template location [$templatePath] does not exist")
    }

    new Configuration(
        outputEncoding: ENCODING,
        directoryForTemplateLoading: path)
  }

  private Configuration buildClasspathConfiguration() {
    new Configuration(
        outputEncoding: ENCODING,
        templateLoader: new ClassTemplateLoader()
    )
  }

  /**
   * Loads template by name from classpath.
   *
   * @param templatePath Path to the template for classpath search,
   *                     e.g. {@code pkg0/pkg1/template.ftl}
   */
  protected Template template(String templatePath) {
    assert templateNames.contains(templatePath) :
        'Template missing from the eager initialization list'

    try {
      return configuration.getTemplate(templatePath, ENCODING)
    } catch (IOException e) {
      throw new RuntimeException("Template not found for URI: $templatePath", e)
    }
  }

  protected String processTemplate(String templatePath, Map<String, Object> data) {
    return processTemplate(template(templatePath), data)
  }

  protected String processTemplate(Template template, Map<String, Object> data) {
    try {
      final params = data + [assertions: templateAssertions as Object] as Map<String, Object>
      new StringWriter().with {
        template.process(params, it)
        it.toString()
      }

    } catch (TemplateException e) {
      throw new RuntimeException("Invalid template: $template.name", e)

    } catch (IOException e) {
      throw new RuntimeException("Error during form generation using template: $template.name", e)
    }
  }

  /**
   * All the known template names, used for eager loading
   * to provoke any validation errors on initialization.
   */
  protected abstract List<String> getTemplateNames()

  protected <T extends TemplateAssertions> T getTemplateAssertions() {
    (T) new TemplateAssertions() {
      @Override void fail(String reason) { assert false: reason }
      @Override void fail() { assert false }
    }
  }

  static interface TemplateAssertions {
    void fail(String reason)
    void fail()
  }
}
