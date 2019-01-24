package com.benio.binder.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * A set of all the bindings requested by a single type.
 */
public class BindingSet {
    // 绑定的类
    private final TypeName targetTypeName;
    // 生成的类
    private final ClassName bindingClassName;
    private Map<Integer, ViewBinding> viewIdMap = new LinkedHashMap<>();

    public BindingSet(TypeName targetTypeName, ClassName bindingClassName) {
        this.targetTypeName = targetTypeName;
        this.bindingClassName = bindingClassName;
    }

    JavaFile brewJava() {
        TypeSpec bindingConfiguration = createType();
        return JavaFile.builder(bindingClassName.packageName(), bindingConfiguration)
                .addFileComment("Generated code from Binder. Do not modify!")
                .build();
    }

    private TypeSpec createType() {
        // public class MainActivity_ViewBinding
        TypeSpec.Builder result = TypeSpec.classBuilder(bindingClassName.simpleName())
                .addModifiers(Modifier.PUBLIC);

        // public static void bind(MainActivity target)
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(targetTypeName, "target");

        // target.title = (TextView)target.findViewById(R.id.title);
        Collection<ViewBinding> viewBindings = Collections.unmodifiableCollection(viewIdMap.values());
        for (ViewBinding viewBinding : viewBindings) {
            methodBuilder.addStatement("target.$L = ($T)target.findViewById($L)",
                    viewBinding.getName(), viewBinding.getType(), viewBinding.getId());
        }

        result.addMethod(methodBuilder.build());
        return result.build();
    }

    public void addField(ViewBinding viewBinding) {
        viewIdMap.put(viewBinding.getId(), viewBinding);
    }

}
