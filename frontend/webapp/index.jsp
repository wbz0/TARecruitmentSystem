<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Group Project Home</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 40px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            max-width: 600px;
            margin: 0 auto;
        }
        h1 { color: #333; text-align: center; }
        .menu {
            list-style: none;
            padding: 0;
            margin-top: 30px;
        }
        .menu li {
            margin: 15px 0;
        }
        .menu a {
            display: block;
            padding: 15px 20px;
            background: #f8f9fa;
            color: #333;
            text-decoration: none;
            border-radius: 6px;
            transition: all 0.3s;
        }
        .menu a:hover {
            background: #667eea;
            color: white;
            transform: translateX(5px);
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Software Engineering Group Project</h1>
        <ul class="menu">
            <li><a href="jsp/welcome.jsp">Welcome JSP Page</a></li>
            <li><a href="hello">Hello Servlet</a></li>
        </ul>
    </div>
</body>
</html>
