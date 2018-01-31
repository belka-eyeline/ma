<#macro org_header>
  <div style="width: 100%; text-align: center; font-weight: bolder">
    <span style="font-weight: bolder">ПРАВИТЕЛЬСТВО МОСКВЫ</span>

    <div style="height: 0.2em"></div>
    Государственное казенное учреждение города Москвы «Администратор Московского парковочного
    пространства»
    <br/>
    (ГКУ «АМПП»)
  </div>
  <hr/>
</#macro>

<#------------------------------------------------------------------------------------------------->

<#macro doc_header resolutionDate docNumber declarantName declarantSnils='001-870-419 12'>
  <#-- @ftlvariable name="resolutionDate" type="java.lang.String" -->
  <#-- @ftlvariable name="docNumber"      type="java.lang.String" -->
  <#-- @ftlvariable name="declarantName"  type="java.lang.String" -->
  <#-- @ftlvariable name="declarantSnils" type="java.lang.String" -->

  <table class="bordered">
    <tr>
      <td class="bordered" style="text-align: center">
        <span>
          ${resolutionDate}
        </span>
        <br/>

        <span>
          № ${docNumber}
        </span>
      </td>
      <td style="text-align: center">
        <span class="placeholder">
          ${declarantName}
        </span>
        <br/>

        <span class="label">
          (Ф.И.О. заявителя)
        </span>

        <br/><br/>

        <#if declarantSnils?? && declarantSnils?has_content>
          <span class="placeholder">
            ${declarantSnils}
          </span>
          <br/>
          <span class="label">
            (СНИЛС заявителя)
          </span>
        </#if>

      </td>
    </tr>
  </table>
</#macro>

<#------------------------------------------------------------------------------------------------->

<#macro document_authored_by>

  <#-- Note: tables are (partially) supported. -->
  <table class="bordered" style="font-size: small">
    <tr>
      <td class="bordered">
        <b>
          Начальник отдела реализации государственных услуг
        </b>
      </td>
      <td class="bordered" style="text-align: right; vertical-align: bottom">
        <b>
          Климов В.В.
        </b>
      </td>
    </tr>
  </table>

</#macro>

<#------------------------------------------------------------------------------------------------->

<#macro document_body assertions shouldNeverHappen>
  <#-- @ftlvariable name="assertions"
                    type="mobi.eyeline.utils.pdfgenerator.templates.BaseTemplateService.TemplateAssertions" -->

  <#-- @ftlvariable name="shouldNeverHappen"
                    type="boolean" -->

  <#if shouldNeverHappen>
    ${assertions.fail()}

  <#else>
    <p class="resolution">
      ${date} в вашу обширную коллекцию картинок с енотами была добавлена еще одна.
      Просим вас радоваться и наслаждаться.
    </p>

  </#if>

</#macro>
