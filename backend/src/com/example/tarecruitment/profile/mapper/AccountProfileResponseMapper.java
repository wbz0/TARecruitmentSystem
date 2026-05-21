package com.example.tarecruitment.profile.mapper;

import com.example.tarecruitment.auth.model.User;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AccountProfileResponseMapper - Account profile response conversion utility.
 *
 * Shared sidebar/top bar only need stable field names; account avatar itself is requested separately via /api/me/avatar.
 */
public final class AccountProfileResponseMapper {

    private AccountProfileResponseMapper() {
    }

    public static Map<String, Object> toPayload(User user, String sharedRealName, boolean hasAvatar) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", safeText(user.getUserId()));
        data.put("username", safeText(user.getUsername()));
        data.put("displayName", safeText(user.getDisplayName()));
        // For TA, realName prefers profile fullName to avoid showing different names on account page vs application profile.
        data.put("realName", safeText(sharedRealName));
        data.put("professionalTitle", safeText(user.getProfessionalTitle()));
        data.put("hasAvatar", hasAvatar);
        return data;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }
}
