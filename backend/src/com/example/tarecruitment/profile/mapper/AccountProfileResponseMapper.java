package com.example.tarecruitment.profile.mapper;

import com.example.tarecruitment.auth.model.User;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AccountProfileResponseMapper - 账号资料响应转换工具。
 *
 * 共享侧边栏/顶栏只需要稳定字段名；账号头像本体通过 /api/me/avatar 单独请求。
 */
public final class AccountProfileResponseMapper {

    private AccountProfileResponseMapper() {
    }

    public static Map<String, Object> toPayload(User user, String sharedRealName, boolean hasAvatar) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", safeText(user.getUserId()));
        data.put("username", safeText(user.getUsername()));
        data.put("displayName", safeText(user.getDisplayName()));
        // realName 对 TA 会优先使用档案 fullName，避免账号页和申请档案显示不同名字。
        data.put("realName", safeText(sharedRealName));
        data.put("professionalTitle", safeText(user.getProfessionalTitle()));
        data.put("hasAvatar", hasAvatar);
        return data;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }
}
