package com.benio.binder.compiler;

import com.benio.binder.BindView;
import com.benio.binder.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class BinderProcessor extends AbstractProcessor {
    private Filer mFiler; //文件相关的辅助类，生成JavaSourceCode
    private Elements mElementUtils; //元素相关的辅助类，帮助我们去获取一些元素相关的信息
    private Messager mMessager; //日志相关的辅助类

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        mFiler = env.getFiler();
        mElementUtils = env.getElementUtils();
        mMessager = env.getMessager();
    }

    /**
     * @return 指定哪些注解需要被注解处理器注册
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    /**
     * @return 指定使用的Java版本。通常返回SourceVersion.latestSupported()
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        long startMillis = System.currentTimeMillis();
        note(null, "start processing");

        Map<TypeElement, BindingSet> bindingMap = findAndParseTargets(env);
        for (Map.Entry<TypeElement, BindingSet> entry : bindingMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingSet bindingSet = entry.getValue();
            try {
                bindingSet.brewJava().writeTo(mFiler);
            } catch (IOException e) {
                error(typeElement, "Unable to write binding for type %s: %s", typeElement, e.getMessage());
            }
        }

        long endMillis = System.currentTimeMillis();
        note(null, "end processing, cost time: %d", endMillis - startMillis);

        return true;
    }

    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, BindingSet> bindingMap = new LinkedHashMap<>();

        for (Element element : env.getElementsAnnotatedWith(BindView.class)) {
            try {
                parseBindView(element, bindingMap);
            } catch (Exception e) {
                logParsingError(element, BindView.class, e);
            }
        }

        for (Element element : env.getElementsAnnotatedWith(OnClick.class)) {
            try {
                parseOnClick(element, bindingMap);
            } catch (Exception e) {
                logParsingError(element, OnClick.class, e);
            }
        }

        return bindingMap;
    }

    private void parseBindView(Element element, Map<TypeElement, BindingSet> bindingMap) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        int id = element.getAnnotation(BindView.class).value();
        String name = element.getSimpleName().toString();
        TypeName type = TypeName.get(element.asType());

        BindingSet bindingSet = getOrCreateBindingSet(bindingMap, enclosingElement);
        bindingSet.addField(id, new FieldViewBinding(name, type));
        note(element, "id: %d, name: %s, type: %s", id, name, type.toString());
    }

    private void parseOnClick(Element element, Map<TypeElement, BindingSet> bindingMap) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        int[] ids = element.getAnnotation(OnClick.class).value();
        String name = element.getSimpleName().toString();
        ExecutableElement executableElement = (ExecutableElement) element;
        TypeMirror returnType = executableElement.getReturnType();
        List<? extends VariableElement> methodParameters = executableElement.getParameters();

        boolean hasError = false;
        String returnTypeStr = "void";
        if (!returnType.toString().equals(returnTypeStr)) {
            error(element, "@%s methods must have a '%s' return type. (%s.%s)",
                    OnClick.class.getSimpleName(), returnTypeStr,
                    enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        if (hasError) {
            return;
        }

        BindingSet bindingSet = getOrCreateBindingSet(bindingMap, enclosingElement);
        for (int id : ids) {
            //bindingSet.addMethod(id, new MethodViewBinding());
        }
        note(element, "ids: %s, name: %s, returnType: %s", Arrays.toString(ids), name, returnType.toString());
    }

    private BindingSet getOrCreateBindingSet(Map<TypeElement, BindingSet> bindingMap, TypeElement enclosingElement) {
        BindingSet bindingSet = bindingMap.get(enclosingElement);
        if (bindingSet == null) {
            TypeMirror typeMirror = enclosingElement.asType();
            TypeName targetType = TypeName.get(typeMirror);

            String packageName = mElementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
            String className = enclosingElement.getQualifiedName().toString().substring(
                    packageName.length() + 1).replace('.', '$');
            ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");

            bindingSet = new BindingSet(targetType, bindingClassName);
            bindingMap.put(enclosingElement, bindingSet);
        }
        return bindingSet;
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation,
                                 Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        mMessager.printMessage(kind, message, element);
    }
}

