package com.benio.binder.compiler;

/**
 * A set of all the bindings requested by a single view.
 * 一个View只有一个id和一个类型，但是可能绑定多个方法
 */
public class ViewBinding {
    // view id
    private final int id;
    // id对应绑定的变量，可为空
    private FieldViewBinding fieldBinding;

    public ViewBinding(int id) {
        this.id = id;
    }

    public void setFieldBinding(FieldViewBinding field) {
        this.fieldBinding = field;
    }

    public FieldViewBinding getFieldBinding() {
        return fieldBinding;
    }

    public void addMethodBinding(MethodViewBinding methodBinding) {

    }

    public int getId() {
        return id;
    }
}
