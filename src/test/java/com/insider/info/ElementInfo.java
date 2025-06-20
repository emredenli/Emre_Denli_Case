package com.insider.info;

import lombok.Getter;
@Getter
public class ElementInfo {
    protected String key;
    protected String value;
    protected String type;

    @Override
    public String toString() {
        return "Elements[" + "keyword=" + key + ",locatorType=" + type + ",locatorValue=" + value + "]";
    }
}