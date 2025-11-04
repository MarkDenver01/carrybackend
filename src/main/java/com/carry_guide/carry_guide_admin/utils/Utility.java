package com.carry_guide.carry_guide_admin.utils;

import com.carry_guide.carry_guide_admin.dto.enums.RoleState;

import java.util.Map;

public class Utility {
    public static boolean isStringNullOrEmpty(final String str) {
        return (str == null || str.isEmpty());
    }

    // define keywords for role state
    private static final Map<String, RoleState> ROLE_KEYWORDS = Map.ofEntries(
            Map.entry("rider", RoleState.DRIVER),
            Map.entry("driver", RoleState.DRIVER),
            Map.entry("customer", RoleState.CUSTOMER),
            Map.entry("admin", RoleState.ADMIN)
    );


    /**
     * Maps a given string to its corresponding RoleState based on keywords.
     *
     * @param input the input string (e.g. "rider_account", "customer_portal")
     * @return matching RoleState or null if no match found
     */
    public static RoleState getRoleState(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.toLowerCase();

        return ROLE_KEYWORDS.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
