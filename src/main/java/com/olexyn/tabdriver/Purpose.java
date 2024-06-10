package com.olexyn.tabdriver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public record Purpose(String name) {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Purpose && ((Purpose) obj).name.equals(name);
    }

}
