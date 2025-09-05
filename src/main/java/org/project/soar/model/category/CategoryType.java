package org.project.soar.model.category;

import java.util.Arrays;
import java.util.Optional;

public enum CategoryType {
    JOB(0, "일자리"),
    HOUSING(1, "주거"),
    EDUCATION(2, "교육"),
    WELFARE(3, "복지문화");

    private final int code;
    private final String name;

    CategoryType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Optional<CategoryType> fromName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.name.equals(name))
                .findFirst();
    }

    public static Optional<CategoryType> fromCode(int code) {
        return Arrays.stream(values())
                .filter(c -> c.code == code)
                .findFirst();
    }
}
