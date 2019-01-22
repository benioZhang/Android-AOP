package com.benio.binder.compiler;

import com.benio.binder.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
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
/*        for (Map.Entry<TypeElement, BindingSet> entry : bindingMap.entrySet()) {
            TypeElement typeElement = entry.getKey();
            BindingSet bindingSet = entry.getValue();
            try {
                bindingSet.brewJava().writeTo(mFiler);
            } catch (IOException e) {
                error(typeElement, "Unable to write binding for type %s: %s", typeElement, e.getMessage());
            }
        }*/

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

        return bindingMap;
    }

    private void parseBindView(Element element, Map<TypeElement, BindingSet> bindingMap) {
        // 获取包名
        PackageElement packageElement = mElementUtils.getPackageOf(element);
        String packageName = packageElement.getQualifiedName().toString();
        // 获取包装类类型
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String className = classElement.getQualifiedName().toString();
        // 获取注解的成员变量名
        String varName = element.getSimpleName().toString();
        // 获取注解的成员变量类型
        String varType = element.asType().toString();
        // 获取该注解的值
        int value = element.getAnnotation(BindView.class).value();
        note(element, "package: %s,\n class: %s,\n var: %s,\n type: %s,\n value: %d.",
                packageName, className, varName, varType, value);

        List<ViewBinding> viewBindings = new ArrayList<>();
        BindingSet bindingSet = new BindingSet(TypeName.get(element.asType()), ClassName.get(classElement), viewBindings);
        bindingMap.put(classElement, bindingSet);
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

