<#-- @ftlvariable name="date" type="java.lang.String" -->
<#-- @ftlvariable name="docNumber"      type="java.lang.String" -->
<#-- @ftlvariable name="declarantName"  type="java.lang.String" -->

<#-- Implicit -->
<#-- @ftlvariable name="assertions"
                  type="mobi.eyeline.utils.pdfgenerator.templates.BaseTemplateService.TemplateAssertions" -->

<#--
  Note: you might include other templates using relative classpath location.
-->
<#include "base.ftl"/>
<#include "styles.ftl"/>

<!DOCTYPE html>

<#--
  Note: specifying global font-family to some known value as eagerly as possible is crucial as
  FOP won't handle unknown fonts.
-->

<#--noinspection CssNoGenericFontName-->
<html xmlns="http://www.w3.org/1999/xhtml" style="font-family: Arial">
<head>
  <meta charset="utf-8"/>

  <!-- Note: emitting `styles' HTML block. -->
  <@common_styles/>
</head>
<body>

<#-- Note: calling macro with no parameters. -->
<@org_header/>

<#-- Note: calling with default parameters. -->
<@doc_header date docNumber declarantName/>

<#-- Note: it's a good way to emulate a horizontal padding. -->
<div style="height: 1em"></div>

<div style="text-align: center; width: 100%">
  У В Е Д О М Л Е Н И Е
</div>

<@document_body assertions false/>

<@document_authored_by/>

<div style="height: 0.3em"></div>

<#-- Note: classpath ref is implicitly handled. -->
<img src="/mobi/eyeline/utils/pdfgenerator/pdf/logo.bmp"/>

</body>
</html>