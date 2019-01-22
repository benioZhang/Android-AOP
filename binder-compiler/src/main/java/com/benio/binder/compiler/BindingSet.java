package com.benio.binder.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * A set of all the bindings requested by a single type.
 */
public class BindingSet {
    private TypeName targetTypeName;
    private ClassName bindingClassName;
    private List<ViewBinding> viewBindings;

    public BindingSet(TypeName targetTypeName, ClassName bindingClassName, List<ViewBinding> viewBindings) {
        this.targetTypeName = targetTypeName;
        this.bindingClassName = bindingClassName;
        this.viewBindings = Collections.unmodifiableList(viewBindings);
    }

    JavaFile brewJava() {
        TypeSpec bindingConfiguration = createType();
        return JavaFile.builder(bindingClassName.packageName(), bindingConfiguration)
                .addFileComment("Generated code from Binder. Do not modify!")
                .build();
    }

    private TypeSpec createType() {
        TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(Modifier.PUBLIC);

        return result.build();
    }
}
