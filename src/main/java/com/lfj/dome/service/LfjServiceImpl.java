package com.lfj.dome.service;

import com.lfj.dome.framework.annotation.LService;

@LService
public class LfjServiceImpl implements ILfjService{

    @Override
    public void add(String name) {
        System.out.println("LfjServiceImpl add ... ");
    }

    @Override
    public void delete(Integer id) {
        System.out.println("LfjServiceImpl...");
    }

    @Override
    public String select(String id) {
        return "LfjServiceImpl";
    }
}
