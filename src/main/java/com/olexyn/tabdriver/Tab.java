package com.olexyn.tabdriver;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Tab {

    String handle;
    String name;
    String url;
    String purpose;

    public Tab(String handle) {
        this.handle = handle;
    }


}
