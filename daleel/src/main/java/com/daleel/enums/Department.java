package com.daleel.enums;

public enum Department {
    MATHEMATICS("Mathematics", "M"),
    COMPUTER_SCIENCE("Computer Science", "CS"),
    PHYSICS("Physics", "PH"),
    CHEMISTRY("Chemistry", "CH"),
    BIOLOGY("Biology", "BI"),
    ENGINEERING("Engineering", "EN"),
    BUSINESS("Business", "BS"),
    MEDICINE("Medicine", "MD");    

    private final String displayName;
    private final String code;

    Department(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public static Department fromDisplayName(String displayName) {
        for (Department department : values()) {
            if (department.getDisplayName().equalsIgnoreCase(displayName)) {
                return department;
            }
        }
        throw new IllegalArgumentException("Invalid department: " + displayName);
    }
}