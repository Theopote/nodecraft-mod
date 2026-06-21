package com.nodecraft.core.exception;

/**
 * Expression parse or evaluation failures in {@code ExpressionNode}.
 */
public class ExpressionEvaluationException extends NodeValidationException {

    public ExpressionEvaluationException(String message) {
        super(message);
    }
}
