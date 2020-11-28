package io.github.bluelhf.calculator;

public abstract class Operator {
    public abstract char key();
    public abstract double get(double a, double b);
}
