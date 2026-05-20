package com.example.tarecruitment.job.web;

import com.example.tarecruitment.auth.model.User;
import com.example.tarecruitment.common.api.ApiRoutes;
import com.example.tarecruitment.common.service.ServiceResult;
import com.example.tarecruitment.common.util.Logger;
import com.example.tarecruitment.common.web.ApiResponses;
import com.example.tarecruitment.job.mapper.JobRequestMapper;
import com.example.tarecruitment.job.service.JobService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * JobServlet - 职位 API 入口。
 *
 * 路径：
 * - GET    /api/jobs：TA 职位列表、MO dashboard 列表。
 * - GET    /api/jobs/{jobId}：TA/MO 职位详情。
 * - POST   /api/jobs：MO 发布职位。
 * - PUT    /api/jobs/{jobId}：MO 更新自己发布的职位。
 * - DELETE /api/jobs/{jobId}：MO 删除自己发布的职位。
 *
 * Servlet 保持薄：只解析路径/参数、取得当前用户、调用 JobService、写统一 JSON。
 */
@WebServlet(urlPatterns = {ApiRoutes.JOBS, ApiRoutes.JOBS + "/*"})
public class JobServlet extends HttpServlet {

    private static final String[] JOB_FIELDS = {
            // POST 创建职位只接受这些前端表单字段，避免无关 request 参数进入业务对象。
            "title",
            "courseCode",
            "courseName",
            "description",
            "requiredSkills",
            "positions",
            "weeklyHours",
            "workStartDate",
            "workEndDate",
            "salary",
            "deadline",
            "status"
    };

    private JobService jobService;

    @Override
    public void init() throws ServletException {
        jobService = JobService.getInstance();
        Logger.i("JobServlet", "JobServlet initialized");
    }

    /**
     * 查询职位列表或单个职位详情。
     *
     * 前端来源：
     * - TA 职位列表页请求列表和详情；
     * - MO dashboard 请求自己发布的职位列表。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String jobId = JobRequestMapper.pathJobId(request.getPathInfo());
            if (!jobId.isEmpty()) {
                write(response, jobService.detail(jobId));
                return;
            }

            // 列表筛选参数来自 TA job-list.js 和 MO dashboard.js。
            write(response, jobService.list(
                    request.getParameter("courseCode"),
                    request.getParameter("status"),
                    request.getParameter("keyword"),
                    request.getParameter("moId")
            ));
        } catch (Exception e) {
            Logger.e("JobServlet", "Error retrieving jobs", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * 创建职位，只接受 /api/jobs 根路径 POST。
     *
     * 权限、字段校验和 CSV 写入都在 JobService 中处理，这里只负责
     * 收集白名单参数并转交。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (!JobRequestMapper.pathJobId(request.getPathInfo()).isEmpty()) {
                ApiResponses.methodNotAllowed(response);
                return;
            }
            write(response, jobService.create(getCurrentUser(request), JobRequestMapper.requestParameters(request, JOB_FIELDS)));
        } catch (Exception e) {
            Logger.e("JobServlet", "Unexpected error during job creation", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * 更新职位详情。
     *
     * 该接口由 MO dashboard 的编辑弹窗调用，PUT body 需要 mapper 手动解析，
     * 避免不同 Servlet 容器对 PUT 表单参数支持不一致。
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String jobId = JobRequestMapper.pathJobId(request.getPathInfo());
            // PUT 请求体是 x-www-form-urlencoded，request.getParameter 在部分容器里不会自动解析。
            Map<String, String> parameters = JobRequestMapper.formParameters(request);
            write(response, jobService.update(getCurrentUser(request), jobId, parameters));
        } catch (Exception e) {
            Logger.e("JobServlet", "Unexpected error during job update", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * 删除职位。
     *
     * 是否允许删除由 JobService 根据当前登录用户和职位发布者判断，
     * Servlet 不直接做业务权限推断。
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            write(response, jobService.delete(getCurrentUser(request), JobRequestMapper.pathJobId(request.getPathInfo())));
        } catch (Exception e) {
            Logger.e("JobServlet", "Unexpected error during job deletion", e);
            ApiResponses.serverError(response, "An error occurred. Please try again later.");
        }
    }

    /**
     * 把 service 层结果转成统一 { success, message, data } JSON。
     */
    private void write(HttpServletResponse response, ServiceResult result) throws IOException {
        ApiResponses.write(response, result.getStatusCode(), result.isSuccess(), result.getMessage(), result.getData());
    }

    /**
     * 只从 session 取当前用户，不在 Servlet 内重复写登录/角色逻辑。
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
}
