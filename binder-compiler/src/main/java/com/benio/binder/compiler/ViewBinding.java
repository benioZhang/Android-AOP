package com.benio.binder.compiler;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A set of all the bindings requested by a single view.
 * 一个View只有一个id和一个类型，但是可能绑定多个方法
 */
final class ViewBinding {
    // view id
    private final int id;
    // id对应绑定的变量，可为空
    private FieldViewBinding fieldBinding;
    // 注解类型和绑定的方法，一对多，因为像TextWatcher是可以添加多个的
    private Map<Class<? extends Annotation>, Set<MethodViewBinding>> methodBindings = new LinkedHashMap<>();

    public ViewBinding(int id) {
        this.id = id;
    }

    public void setFieldBinding(FieldViewBinding field) {
        this.fieldBinding = field;
    }

    public FieldViewBinding getFieldBinding() {
        return fieldBinding;
    }

    public Map<Class<? extends Annotation>, Set<MethodViewBinding>> getMethodBindings() {
        return methodBindings;
    }

    public void addMethodBinding(Class<? extends Annotation> annotationClass, MethodViewBinding methodBinding) {
        Set<MethodViewBinding> methods = methodBindings.get(annotationClass);
        if (methods == null) {
            methods = new LinkedHashSet<>();
            methodBindings.put(annotationClass, methods);
        }
        methods.add(methodBinding);
    }

    public int getId() {
        return id;
    }
}
