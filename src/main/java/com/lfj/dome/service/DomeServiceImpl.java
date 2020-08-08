package com.lfj.dome.service;

import com.lfj.dome.annotation.LService;

@LService
public class DomeServiceImpl implements IDomeService{

    public String get(String name) {
        return "Hello " + name;
    }

}
