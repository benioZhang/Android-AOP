package com.benio.binder.compiler;

import com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.List;

final class MethodViewBinding {
    private String name;
    private List<TypeName> parameters;

    public MethodViewBinding(String name, List<TypeName> parameters) {
        this.name = name;
        this.parameters = Collections.unmodifiableList(parameters);
    }

    public List<TypeName> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }
}
