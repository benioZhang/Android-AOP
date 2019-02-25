package com.benio.ast.compiler;

import com.benio.ast.TrackEvent;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class TrackEventHelper {
    private TreeMaker mTreeMaker;
    private Names mNames;
    private ExecutableElement mExecutableElement;

    public TrackEventHelper(TreeMaker treeMaker, Names names, ExecutableElement executableElement) {
        mTreeMaker = treeMaker;
        mNames = names;
        mExecutableElement = executableElement;
    }

    // 创建 域/方法 的多级访问，如java.lang.System.out.println
    private JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = mTreeMaker.Ident(mNames.fromString(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = mTreeMaker.Select(expr, mNames.fromString(componentArray[i]));
        }
        return expr;
    }

    // 构造语句: com.benio.ast.track.Tracker.onEvent("main_onCreate_event");
    // 如果构造成Tracker.onEvent("main_onCreate_event")这样的语句
    // 除非Tracker和要被插入语句的类是同一个包下，否则会报`找不到符号`的错误
    // 因为不会自动import类的，所以引用类时必须用全路径
    public JCTree.JCStatement createStatement(TrackEvent trackEvent) {
        // 参数类型列表
        List<JCTree.JCExpression> typeArgs = List.of(memberAccess(String.class.getName()));
        // 调用语句
        TypeElement typeElement = (TypeElement) mExecutableElement.getEnclosingElement();
        JCTree.JCExpression method = memberAccess(
                typeElement.getQualifiedName().toString()
                        + "." + mExecutableElement.getSimpleName().toString());
        // 参数值列表
        List<JCTree.JCExpression> args = List.<JCTree.JCExpression>of(mTreeMaker.Literal(trackEvent.value()));
        // 埋点调用语句
        return mTreeMaker.Exec(mTreeMaker.Apply(typeArgs, method, args));
    }
}
