package com.gdx.game2048.manager;

import java.util.Map;

public class MyEntry <String, Integer> implements Map.Entry<String, Integer> {
    private final String key;
    private Integer value;

    public MyEntry(String key, Integer value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public Integer setValue(Integer value) {
        Integer old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
