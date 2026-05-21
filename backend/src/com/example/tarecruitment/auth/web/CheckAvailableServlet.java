package com.example.tarecruitment.auth.web;

import com.example.tarecruitment.auth.dao.UserDao;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * CheckAvailableServlet - Check if username or email is available.
 *
 * Current frontend entry: real-time availability validation in register.jsp and admin-invite.jsp.
 * Examples:
 * - type=username, value=john
 * - type=email, value=a@b.com
 * Response: {"success":true,"available":true/false}
 */
@WebServlet(ApiRoutes.AUTH_AVAILABILITY)
public class CheckAvailableServlet extends HttpServlet {

    private static final int USERNAME_MAX_LENGTH = 20;
    private static final int EMAIL_MAX_LENGTH = 100;

    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String type = request.getParameter("type");
        String value = request.getParameter("value");

        if (type == null || value == null) {
            writeAvailable(response, false);
            return;
        }

        type = type.trim().toLowerCase();
        value = value.trim();

        if (value.isEmpty()) {
            writeAvailable(response, false);
            return;
        }

        boolean available;
        if ("username".equals(type)) {
            // Only return available boolean here; detailed format errors are still returned by registration endpoint.
            if (value.length() > USERNAME_MAX_LENGTH) {
                writeAvailable(response, false);
                return;
            }
            available = !userDao.existsByUsername(value);
        } else if ("email".equals(type)) {
            if (value.length() > EMAIL_MAX_LENGTH) {
                writeAvailable(response, false);
                return;
            }
            available = !userDao.existsByEmail(value);
        } else {
            writeAvailable(response, false);
            return;
        }

        writeAvailable(response, available);
    }

    private void writeAvailable(HttpServletResponse response, boolean available) throws IOException {
        // Frontend only cares about data.available, backend validation details are not shown.
        ApiResponses.write(response, 200, true, null,
                Map.of("available", available));
    }
}
