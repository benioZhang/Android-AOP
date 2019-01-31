package com.benio.ast.compiler;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public class ClearLogTranslator extends TreeTranslator {
    public static final String LOG_TAG = "Log.";
    private Messager mMessager;
    private String mClassName;

    public ClearLogTranslator(Messager messager, String className) {
        mMessager = messager;
        mClassName = className;
    }

    @Override
    public void visitBlock(JCTree.JCBlock jcBlock) {
        super.visitBlock(jcBlock);
        // 扫描Log语句，粒度为语句块
        // 获取"{ }"里面的语句
        List<JCTree.JCStatement> statements = jcBlock.stats;
        if (statements == null || statements.isEmpty()) {
            return;
        }
        // 这里的List不是我们一向使用的List
        List<JCTree.JCStatement> newStatements = List.nil();
        for (JCTree.JCStatement statement : statements) {
            // 判断是否为Log语句
            if (statement.toString().startsWith(LOG_TAG)) {
                note("In %s, statement: '%s' will be removed!!! ", mClassName,
                        statement.toString());
            } else {
                newStatements = newStatements.append(statement);
            }
        }
        jcBlock.stats = newStatements;
    }

    private void note(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        if (mMessager != null) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, message, null);
        }
    }
}
