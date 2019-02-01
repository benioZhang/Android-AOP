package com.benio.ast.compiler;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

public abstract class JCStatementFilter extends TreeTranslator {

    @Override
    public final void visitBlock(JCTree.JCBlock jcBlock) {
        super.visitBlock(jcBlock);
        // 获取"{ }"里面的语句
        List<JCTree.JCStatement> statements = jcBlock.stats;
        if (statements == null || statements.isEmpty()) {
            return;
        }
        // 这里的List不是我们一向使用的List
        List<JCTree.JCStatement> stats = List.nil();
        for (JCTree.JCStatement statement : statements) {
            if (accepts(jcBlock, statement)) {
                stats = stats.append(statement);
            }
        }
        jcBlock.stats = stats;
    }

    /**
     * @param statement statement in block
     * @return Whether statement will be accepted.
     */
    protected abstract boolean accepts(JCTree.JCBlock jcBlock, JCTree.JCStatement statement);
}
