package com.olexyn.tabdriver;

public record Purpose(String name) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Purpose && ((Purpose) obj).name.equals(name);
    }

}
