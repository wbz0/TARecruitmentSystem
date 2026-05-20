package com.example.tarecruitment.admin.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.admin.service.InviteCodeService;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.web.ApiResponses;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AdminCurrentInviteCodeServlet - 当前邀请码查询与主动刷新接口。
 *
 * 对应 /jsp/admin/invite.jsp 和 js/admin/admin-invite-management.js。
 * 这是当前管理员页面实际展示的邀请方式：管理员看到短码，必要时手动刷新。
 *
 * GET  /api/admin/invitations/current-code  返回当前码和剩余秒数（需 ADMIN）
 * POST /api/admin/invitations/current-code  主动轮换，返回新码（需 ADMIN）
 */
@WebServlet(ApiRoutes.ADMIN_CURRENT_INVITATION_CODE)
public class AdminCurrentInviteCodeServlet extends HttpServlet {

    private InviteCodeService inviteCodeService;

    @Override
    public void init() {
        inviteCodeService = InviteCodeService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        User user = requireAdmin(request, response);
        if (user == null) return;

        ApiResponses.write(response, 200, true, "OK",
                ApiResponses.objectMap(
                        "code", inviteCodeService.getCurrentCode(),
                        "secondsRemaining", inviteCodeService.getSecondsRemaining()
                ));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        User user = requireAdmin(request, response);
        if (user == null) return;

        String newCode = inviteCodeService.forceRotate();
        ApiResponses.write(response, 200, true, "Code rotated",
                ApiResponses.objectMap(
                        "code", newCode,
                        "secondsRemaining", inviteCodeService.getSecondsRemaining()
                ));
    }

    private User requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            ApiResponses.write(response, 401, false, "Please login first", null);
            return null;
        }
        if (user.getRole() != User.Role.ADMIN) {
            ApiResponses.write(response, 403, false, "Admin access required", null);
            return null;
        }
        return user;
    }
}
