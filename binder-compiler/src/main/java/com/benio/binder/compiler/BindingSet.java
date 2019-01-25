package com.benio.binder.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * A set of all the bindings requested by a single type.
 */
class BindingSet {
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
                .addFileComment("Generated code from ViewBinder. Do not modify!")
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

        Collection<ViewBinding> viewBindings = Collections.unmodifiableCollection(viewIdMap.values());
        for (ViewBinding viewBinding : viewBindings) {
            FieldViewBinding fieldBinding = viewBinding.getFieldBinding();
            if (fieldBinding != null) {
                // target.title = (TextView)target.findViewById(R.id.title);
                methodBuilder.addStatement("target.$L = ($T)target.findViewById($L)",
                        fieldBinding.getName(), fieldBinding.getType(), viewBinding.getId());
            }

            Map<Class<? extends Annotation>, Set<MethodViewBinding>> methodBindings = viewBinding.getMethodBindings();
            for (Map.Entry<Class<? extends Annotation>, Set<MethodViewBinding>> e : methodBindings.entrySet()) {
                // TODO generate listener code
            }
        }

        result.addMethod(methodBuilder.build());
        return result.build();
    }

    public void addField(int id, FieldViewBinding fieldBinding) {
        getOrCreateViewBinding(id).setFieldBinding(fieldBinding);
    }

    public void addMethod(int id, Class<? extends Annotation> annotation, MethodViewBinding methodBinding) {
        getOrCreateViewBinding(id).addMethodBinding(annotation, methodBinding);
    }

    private ViewBinding getOrCreateViewBinding(int id) {
        ViewBinding viewBinding = viewIdMap.get(id);
        if (viewBinding == null) {
            viewBinding = new ViewBinding(id);
            viewIdMap.put(id, viewBinding);
        }
        return viewBinding;
    }

}
