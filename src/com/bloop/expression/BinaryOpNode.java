package com.bloop.expression;

import com.bloop.runtime.Environment;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Represents a binary operation between two expressions, such as
 * {@code x + y * 2} or {@code score > 50}.
 * 
 * <h3>Functional Programming — BiFunction Operator Map</h3>
 * Instead of a long if-else chain to handle each operator, this class
 * uses a {@code Map<String, BiFunction<Double, Double, Object>>} to
 * look up the operation. Each operator is stored as a lambda:
 * 
 * <pre>{@code
 *   "+" → (a, b) -> a + b
 *   ">" → (a, b) -> a > b
 * }</pre>
 * 
 * This makes adding new operators trivial — just add an entry to the map.
 * 
 * <h3>Generics</h3>
 * {@code BiFunction<Double, Double, Object>} — takes two Doubles as input
 * and returns Object (either Double for arithmetic or Boolean for
 * comparisons), demonstrating Java generics with functional interfaces.
 * 
 * <h3>Immutability</h3>
 * Left expression, operator, and right expression are all final fields.
 */
public class BinaryOpNode implements Expression {

    private final Expression left;
    private final String operator;
    private final Expression right;

    /**
     * Map of operator symbols to their implementations.
     * 
     * Uses generics: BiFunction<Double, Double, Object>
     * — input types are Double, output is Object to accommodate
     *   both Double (arithmetic) and Boolean (comparison) results.
     * 
     * Uses lambdas: each entry is a concise lambda expression.
     */
    private static final Map<String, BiFunction<Double, Double, Object>> OPERATORS = Map.of(
        "+", (a, b) -> a + b,          // addition
        "-", (a, b) -> a - b,          // subtraction
        "*", (a, b) -> a * b,          // multiplication
        "/", (a, b) -> {               // division with zero-check
            if (b == 0) throw new RuntimeException("Division by zero");
            return a / b;
        },
        ">", (a, b) -> a > b,          // greater than
        "<", (a, b) -> a < b,          // less than
        "==", (a, b) -> a.equals(b)    // equality
    );

    /**
     * Constructs a BinaryOpNode.
     *
     * @param left     the left-hand expression
     * @param operator the operator symbol ("+", "-", "*", "/", ">", "<", "==")
     * @param right    the right-hand expression
     */
    public BinaryOpNode(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Evaluates the binary operation:
     * 1. Evaluate the left expression
     * 2. Evaluate the right expression
     * 3. Look up the operator in the OPERATORS map
     * 4. Apply it to both results
     * 
     * Arithmetic operators return Double; comparison operators return Boolean.
     *
     * @param env the variable store
     * @return the result of the operation
     * @throws RuntimeException if the operator is unknown or operands aren't numbers
     */
    @Override
    public Object evaluate(Environment env) {
        Object leftVal = left.evaluate(env);
        Object rightVal = right.evaluate(env);

        // Both operands must be numbers for binary operations
        if (!(leftVal instanceof Double) || !(rightVal instanceof Double)) {
            throw new RuntimeException(
                "Cannot apply operator '" + operator + "' to non-numeric values: "
                + leftVal + " and " + rightVal
            );
        }

        // Functional programming: look up the operator function and apply it
        BiFunction<Double, Double, Object> operation = OPERATORS.get(operator);
        if (operation == null) {
            throw new RuntimeException("Unknown operator: " + operator);
        }

        return operation.apply((Double) leftVal, (Double) rightVal);
    }

    @Override
    public String toString() {
        return "BinaryOpNode(" + left + " " + operator + " " + right + ")";
    }
}
