package com.benio.ast.compiler;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class ClearLogProcessor extends AbstractProcessor {
    private Trees mTrees;// 抽象语法树
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        mTrees = Trees.instance(env);
        mElementUtils = env.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add("*");
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getRootElements()) {
            // 粒度为类
            if (element.getKind() == ElementKind.CLASS) {
                // 获取包名
                String packageName = mElementUtils.getPackageOf(element).getQualifiedName().toString();
                // 拼接类名
                String className = packageName + "." + element.getSimpleName().toString();

                TreeTranslator translator = new ClearLogTranslator(processingEnv.getMessager(),
                        className);
                JCTree jcTree = (JCTree) mTrees.getTree(element);
                jcTree.accept(translator);
            }
        }
        return true;
    }
}
