<%--
  Created by IntelliJ IDEA.
  User: martin
  Date: 5/10/16
  Time: 10:33 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.byw.stock.house.data.center.web.controllers.BaseController" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%
    String message= (String) request.getAttribute(BaseController.FORWARD_MESSAGE);
    out.println(message);
%>
</body>
</html>
