package com.increff.pos.constants;

public enum UserRole {
    SUPERVISOR,
    OPERATOR;

    public String role() {
        return name();
    }
}
