package com.benio.binder.compiler;

import com.squareup.javapoet.TypeName;

/**
 * A set of all the bindings requested by a single view.
 * 一个View只有一个id和一个类型，但是可能绑定多个方法
 */
public class ViewBinding {
    private final int id;
    private final String name;
    private final TypeName type;

    public ViewBinding(int id, String name, TypeName type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }
}
