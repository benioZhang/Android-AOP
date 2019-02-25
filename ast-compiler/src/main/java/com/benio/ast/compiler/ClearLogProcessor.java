package com.benio.ast.compiler;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class ClearLogProcessor extends AbstractProcessor {
    private static final List<String> LOG_TAGS = Arrays.asList("Log.", "Logger.");
    private Trees mTrees;// 抽象语法树工具集
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
        long startMillis = System.currentTimeMillis();
        note("start processing");
        // 遍历所有类
        for (Element element : roundEnvironment.getRootElements()) {
            // 粒度为类
            if (element.getKind() == ElementKind.CLASS) {
                // 获取包名
                String packageName = mElementUtils.getPackageOf(element).getQualifiedName().toString();
                // 拼接类名
                final String className = packageName + "." + element.getSimpleName().toString();

                TreeTranslator translator = new JCStatementFilter() {
                    @Override
                    protected boolean accepts(JCTree.JCBlock jcBlock, JCTree.JCStatement statement) {
                        // 判断是否为Log语句
                        for (String tag : LOG_TAGS) {
                            if (statement.toString().contains(tag)) {
                                note("In %s, statement: '%s' will be removed!!!", className,
                                        statement.toString());
                                return false;
                            }
                        }
                        return true;
                    }
                };
                JCTree jcTree = (JCTree) mTrees.getTree(element);
                jcTree.accept(translator);
            }
        }

        long endMillis = System.currentTimeMillis();
        note("end processing, cost time: %d", endMillis - startMillis);
        return false;
    }

    private void note(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, null);
    }
}
