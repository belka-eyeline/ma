<%@ page contentType="application/xml; charset=UTF-8" language="java" %>
<page version="2.0">
  <div>
     Okay, you've selected a language and it is: <b><%=request.getParameter("lang")%></b>.
  </div>
  <navigation id="submit">
      <link pageId="index.jsp">Try again</link>
  </navigation>
</page>