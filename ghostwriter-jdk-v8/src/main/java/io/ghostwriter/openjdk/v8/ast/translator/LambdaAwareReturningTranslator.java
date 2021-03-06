package io.ghostwriter.openjdk.v8.ast.translator;

import com.sun.tools.javac.tree.JCTree;
import io.ghostwriter.openjdk.v7.ast.compiler.JavaCompiler;
import io.ghostwriter.openjdk.v7.ast.compiler.JavaCompilerHelper;
import io.ghostwriter.openjdk.v7.ast.translator.ReturningTranslator;


public class LambdaAwareReturningTranslator extends ReturningTranslator {

    private JCTree.JCLambda visitedLambda = null;

    public LambdaAwareReturningTranslator(JavaCompiler javac, JavaCompilerHelper helper) {
        super(javac, helper);
    }

    @Override
    public void visitLambda(JCTree.JCLambda jcLambda) {
        if (!Lambdas.doInstrumentLambdas()) {
            result = jcLambda;
            return;
        }

        final boolean isLambdaBodyWrappedInBlock = jcLambda.body instanceof JCTree.JCBlock;
        if (!isLambdaBodyWrappedInBlock) {
            throw new IllegalStateException("Lambda does not have a body, got: " + jcLambda.body.getClass() + "," + jcLambda.toString());
            // we run the LambdaAwareWrapInBlockTranslator to ensure that all lambdas have a body block.
            // it that is not the case there is an error somewhere else!
        }

        visitedLambda = jcLambda;
        super.visitLambda(jcLambda);
        visitedLambda = null;
    }

    @Override
    protected JCTree.JCLiteral returningExpressionMethodName() {
        if (visitedLambda == null) {
            return super.returningExpressionMethodName();
        }
        return returningExpressionLambdaName();
    }

    private JCTree.JCLiteral returningExpressionLambdaName() {
        final String lambdaName = Lambdas.nameFor(getEnclosingMethod(), visitedLambda);
        return getJavac().literal(lambdaName);
    }

}
