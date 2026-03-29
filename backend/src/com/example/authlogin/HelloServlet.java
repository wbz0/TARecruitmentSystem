package com.example.authlogin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 简单的Hello World Servlet
 * 访问路径: http://localhost:8080/your-app/hello
 */
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<!DOCTYPE html>" +
            "<html><head><title>Hello Servlet</title></head><body>" +
            "<h1>Hello from Servlet!</h1>" +
            "<p>当前时间: " + new java.util.Date() + "</p>" +
            "<p>Java版本: " + System.getProperty("java.version") + "</p>" +
            "</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<!DOCTYPE html>" +
            "<html><head><title>Hello Servlet - POST</title></head><body>" +
            "<h1>Hello, " + (name != null ? name : "Guest") + "!</h1>" +
            "</body></html>");
    }
}
