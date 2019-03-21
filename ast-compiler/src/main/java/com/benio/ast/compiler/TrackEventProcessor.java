package com.benio.ast.compiler;

import com.benio.ast.TrackEvent;
import com.benio.ast.TrackInvoker;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"com.benio.ast.TrackEvent", "com.benio.ast.TrackInvoker"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TrackEventProcessor extends AbstractProcessor {
    private Trees mTrees;// 抽象语法树工具集
    private Names mNames;// 用于创建标识符
    private TreeMaker mTreeMaker;// 用于创建AST节点

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        mTrees = Trees.instance(env);
        Context context = ((JavacProcessingEnvironment) env).getContext();
        mTreeMaker = TreeMaker.instance(context);
        mNames = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        ExecutableElement trackInvokerElement = null;
        for (Element element : roundEnvironment.getElementsAnnotatedWith(TrackInvoker.class)) {
            // 必须是方法
            if (element.getKind() != ElementKind.METHOD) {
                throw new IllegalStateException();
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            Set<Modifier> modifiers = executableElement.getModifiers();
            // 目前只支持public static 方法
            if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) {
                continue;
            }
            java.util.List<? extends VariableElement> parameters = executableElement.getParameters();
            // 目前只支持一个参数
            if (parameters == null || parameters.size() != 1) {
                continue;
            }
            // 埋点方法的参数类型必须和TrackEvent相同
            VariableElement parameter = parameters.get(0);
            if (!parameter.asType().toString().equals(String.class.getName())) {
                continue;
            }
            trackInvokerElement = executableElement;
            note("TrackInvoker %s#%s", element.getEnclosingElement(), element);
            break;
        }

        if (trackInvokerElement == null) {
            return false;
        }

        for (final Element element : roundEnvironment.getElementsAnnotatedWith(TrackEvent.class)) {
            // 必须是方法
            if (element.getKind() != ElementKind.METHOD) {
                throw new IllegalStateException();
            }
            final TrackEvent trackEvent = element.getAnnotation(TrackEvent.class);
            final TrackEventHelper trackEventHelper = new TrackEventHelper(mTreeMaker, mNames,
                    trackInvokerElement);
            JCTree jcTree = (JCTree) mTrees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
                    super.visitMethodDef(jcMethodDecl);
                    // 防止添加语句错误，如不添加，被标注方法内的匿名内部类的方法也会被添加语句
                    if (jcMethodDecl.getName().equals(element.getSimpleName())) {
                        JCTree.JCStatement statement = trackEventHelper.createStatement(trackEvent);
                        note("In %s, method %s will append statement: %s",
                                element.getEnclosingElement(), jcMethodDecl.getName(), statement);
                        jcMethodDecl.body.stats = jcMethodDecl.body.stats.append(statement);
                    }
                }
            });
        }
        return false;
    }

    private void note(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, null);
    }
}
