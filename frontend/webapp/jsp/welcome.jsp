<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
    <title>Welcome JSP</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 40px;
            background-color: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 { color: #333; }
        .info { color: #666; margin: 10px 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Welcome to JSP!</h1>
        <div class="info">
            <p>当前时间: <%= new Date() %></p>
            <p>Java版本: <%= System.getProperty("java.version") %></p>
            <p>Servlet版本: <%= application.getMajorVersion() %>.<%= application.getMinorVersion() %></p>
            <p>Session ID: <%= session.getId() %></p>
        </div>
        <hr>
        <h2>测试表单</h2>
        <form action="<%= request.getContextPath() %>/hello" method="post">
            <input type="text" name="name" placeholder="输入你的名字">
            <button type="submit">提交</button>
        </form>
        <p><a href="<%= request.getContextPath() %>/hello">直接访问Servlet</a></p>
    </div>
</body>
</html>
