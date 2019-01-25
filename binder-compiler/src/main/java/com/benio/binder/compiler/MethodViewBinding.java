package com.benio.binder.compiler;

import com.squareup.javapoet.TypeName;

import java.util.List;

class MethodViewBinding {
    // 方法名
    private String name;
    // 参数
    private List<TypeName> parameters;
    // 返回类型
    private TypeName returnType;

    public MethodViewBinding(String name, List<TypeName> parameters, TypeName returnType) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    public TypeName getReturnType() {
        return returnType;
    }

    public List<TypeName> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }
}
