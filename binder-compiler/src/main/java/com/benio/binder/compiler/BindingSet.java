package com.benio.binder.compiler;

import com.benio.binder.OnClick;
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
    private static final ClassName VIEW = ClassName.get("android.view", "View");
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
                .addParameter(targetTypeName, "target", Modifier.FINAL);

        Collection<ViewBinding> viewBindings = Collections.unmodifiableCollection(viewIdMap.values());
        for (ViewBinding viewBinding : viewBindings) {
            addViewBinding(methodBuilder, viewBinding);
        }

        result.addMethod(methodBuilder.build());
        return result.build();
    }

    private void addViewBinding(MethodSpec.Builder methodBuilder, ViewBinding viewBinding) {
        FieldViewBinding fieldBinding = viewBinding.getFieldBinding();
        if (fieldBinding != null) {
            // target.title = (TextView)target.findViewById(R.id.title);
            methodBuilder.addStatement("target.$L = ($T)target.findViewById($L)",
                    fieldBinding.getName(), fieldBinding.getType(), viewBinding.getId());
        }

        Map<Class<? extends Annotation>, Set<MethodViewBinding>> methodBindings =
                viewBinding.getMethodBindings();
        for (Map.Entry<Class<? extends Annotation>, Set<MethodViewBinding>> e : methodBindings.entrySet()) {
            Class<? extends Annotation> annotationClass = e.getKey();
            Set<MethodViewBinding> methodViewBindings = e.getValue();

            if (OnClick.class.equals(annotationClass)) {
                // 一个View只会有一个OnClick
                MethodViewBinding methodViewBinding = methodViewBindings.iterator().next();
                // new View.OnClickListener() {
                //      @Override
                //      public void onClick(View view) {
                //        target.onClick(view);
                //      }
                //    }
                TypeSpec listener = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ClassName.get("android.view", "View", "OnClickListener"))
                        .addMethod(MethodSpec.methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(void.class)
                                .addParameter(VIEW, "view")
                                // 假设用OnClick绑定的方法都有View参数
                                .addStatement("target.$N($N)", methodViewBinding.getName(), "view")
                                .build())
                        .build();
                if (fieldBinding != null) {
                    methodBuilder.addStatement("target.$L.setOnClickListener($L)", fieldBinding.getName(), listener);
                } else {
                    methodBuilder.addStatement("target.findViewById($L).setOnClickListener($L)", viewBinding.getId(), listener);
                }
            }
        }
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
