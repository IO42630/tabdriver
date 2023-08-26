package com.olexyn.tabdriver;

import lombok.Getter;
import lombok.Setter;

public class Tab {


    @Getter
    @Setter
    String handle;

    @Getter
    @Setter
    String name;

    @Getter
    @Setter
    String url;

    @Getter
    @Setter
    String purpose;


    public Tab(String handle) {
        this.handle = handle;
    }


}
