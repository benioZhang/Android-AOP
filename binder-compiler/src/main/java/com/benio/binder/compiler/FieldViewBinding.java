package com.benio.binder.compiler;

import com.squareup.javapoet.TypeName;

final class FieldViewBinding {
    // 变量名
    private final String name;
    // 类型
    private final TypeName type;

    FieldViewBinding(String name, TypeName type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }
}