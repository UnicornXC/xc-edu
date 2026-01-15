package com.xuecheng.framework.enums;

import lombok.Getter;

@Getter
public enum CmsPageTypeEnum {
    Dynamic("1"),
    Static("0");

    private String code;

    CmsPageTypeEnum(String code) {
        this.code = code;
    }
}
