package com.Task_Management_System.TMS.demo;

/**
 * The shared, public demo login. Anyone can use it to explore Taskly without signing up,
 * which is why its details are not secret and the account itself is protected from edits.
 */
public final class DemoAccount {

    public static final String EMAIL = "demo@taskly.dev";
    public static final String PASSWORD = "demo1234";
    public static final String USERNAME = "Demo User";

    private DemoAccount() {
    }

    public static boolean isDemo(String email) {
        return EMAIL.equalsIgnoreCase(email);
    }
}
