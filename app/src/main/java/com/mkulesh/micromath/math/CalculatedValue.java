/*******************************************************************************
 * microMathematics Plus - Extended visual calculator
 * *****************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.mkulesh.micromath.math;

import com.duy.natural.calc.calculator.evaluator.CalculateTask;
import com.duy.natural.calc.calculator.evaluator.CalculateTask.CancelException;
import com.mkulesh.micromath.formula.views.TermField;
import com.mkulesh.micromath.formula.TermParser;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.ViewUtils;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import java.util.List;

public class CalculatedValue {
    public final static CalculatedValue NaN = new CalculatedValue(ValueType.INVALID, Double.NaN, 0.0);
    public final static CalculatedValue ONE = new CalculatedValue(ValueType.REAL, 1.0, 0.0);
    public final static CalculatedValue MINUS_ONE = new CalculatedValue(ValueType.REAL, -1.0, 0.0);
    private ValueType valueType = ValueType.INVALID;
    private double real = Double.NaN;
    private double imaginary = 0.0;

    /*********************************************************
     * Common methods
     *********************************************************/

    public CalculatedValue() {
        // empty
    }

    public CalculatedValue(ValueType valueType, double real, double imaginary) {
        this.valueType = valueType;
        this.real = real;
        this.imaginary = imaginary;
    }

    public static boolean isInvalidReal(double v) {
        return Double.isNaN(v) || Double.isInfinite(v);
    }

    public ValueType assign(CalculatedValue c) {
        valueType = c.valueType;
        real = c.real;
        imaginary = c.imaginary;
        return valueType;
    }

    public ValueType invalidate(ErrorType errorType) {
        valueType = ValueType.INVALID;
        real = Double.NaN;
        imaginary = 0.0;
        return valueType;
    }

    public ValueType setValue(double real) {
        this.real = real;
        this.imaginary = 0.0;
        valueType = ValueType.REAL;
        return valueType;
    }

    public ValueType setComplexValue(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
        valueType = (imaginary != 0.0) ? ValueType.COMPLEX : ValueType.REAL;
        return valueType;
    }

    public ValueType setComplexValue(Complex c) {
        real = c.getReal();
        imaginary = c.getImaginary();
        valueType = (imaginary != 0.0) ? ValueType.COMPLEX : ValueType.REAL;
        return valueType;
    }

    /*********************************************************
     * Common getters
     *********************************************************/

    public ValueType getValueType() {
        return valueType;
    }

    public boolean isReal() {
        return valueType == ValueType.REAL;
    }

    public boolean isComplex() {
        return valueType == ValueType.COMPLEX;
    }

    public boolean isNaN() {
        switch (valueType) {
            case REAL:
                return isInvalidReal(real);
            case COMPLEX:
                return isInvalidReal(real) || isInvalidReal(imaginary);
            default:
                return true;
        }
    }

    public boolean isZero() {
        switch (valueType) {
            case REAL:
                return real == 0.0;
            case COMPLEX:
                return real == 0.0 && imaginary == 0.0;
            default:
                return false;
        }
    }

    public double getReal() {
        return real;
    }

    public double getImaginary() {
        return imaginary;
    }

    public Complex getComplex() {
        return new Complex(real, imaginary);
    }

    public int getInteger() {
        return (int) Math.floor(real);
    }

    public double getPart(PartType type) {
        return type == PartType.RE ? real : imaginary;
    }

    /*********************************************************
     * Conversion methods
     *********************************************************/

    public String toString() {
        return valueType.toString() + "[" + real + ", " + imaginary + "]";
    }

    public String getResultDescription(DocumentProperties doc) {
        switch (valueType) {
            case INVALID:
                return TermParser.CONST_NAN;
            case REAL:
                if (Double.isNaN(real)) {
                    return TermParser.CONST_NAN;
                }
                return formatValue(real, doc, false);
            case COMPLEX:
                if (Double.isNaN(real) || Double.isNaN(imaginary)) {
                    return TermParser.CONST_NAN;
                }
                return formatValue(real, doc, false) + formatValue(imaginary, doc, true) + "i";
        }
        return "";
    }

    private String formatValue(double value, DocumentProperties doc, boolean addPlusSign) {
        if (Double.isInfinite(value)) {
            if (value < 0) {
                return "-" + TermParser.CONST_INF;
            } else {
                return addPlusSign ? "+" + TermParser.CONST_INF : TermParser.CONST_INF;
            }
        } else {
            final double roundV = ViewUtils.roundToNumberOfSignificantDigits(value, doc.significantDigits);
            if (roundV >= 0 && addPlusSign) {
                return "+" + Double.toString(roundV);
            } else {
                return Double.toString(roundV);
            }
        }
    }

    /*********************************************************
     * Calculation methods
     *********************************************************/

    public void processRealTerm(CalculateTask thread, TermField term) throws CancelException {
        term.getValue(thread, this);
        if (!isReal()) {
            invalidate(ErrorType.NOT_A_REAL);
        }
    }

    public ValueType add(CalculatedValue f, CalculatedValue g) {
        if (f.isComplex() || g.isComplex()) {
            return setComplexValue(f.real + g.real, f.imaginary + g.imaginary);
        } else {
            return setValue(f.real + g.real);
        }
    }

    public ValueType subtract(CalculatedValue f, CalculatedValue g) {
        if (f.isComplex() || g.isComplex()) {
            return setComplexValue(f.real - g.real, f.imaginary - g.imaginary);
        } else {
            return setValue(f.real - g.real);
        }
    }

    public ValueType multiply(CalculatedValue f, CalculatedValue g) {
        if (f.isComplex() || g.isComplex()) {
            return setComplexValue(f.real * g.real - f.imaginary * g.imaginary, f.real * g.imaginary + f.imaginary
                    * g.real);
        } else {
            return setValue(f.real * g.real);
        }
    }

    public ValueType multiply(double f) {
        real *= f;
        imaginary *= f;
        return valueType;
    }

    public ValueType divide(CalculatedValue f, CalculatedValue g) {
        if (f.isComplex() || g.isComplex()) {
            final double c = g.real;
            final double d = g.imaginary;
            if (FastMath.abs(c) < FastMath.abs(d)) {
                final double q = c / d;
                final double denominator = c * q + d;
                return setComplexValue((f.real * q + f.imaginary) / denominator, (f.imaginary * q - f.real)
                        / denominator);
            } else {
                final double q = d / c;
                final double denominator = d * q + c;
                return setComplexValue((f.imaginary * q + f.real) / denominator, (f.imaginary - f.real * q)
                        / denominator);
            }
        } else {
            return setValue(f.real / g.real);
        }
    }

    public ValueType pow(CalculatedValue f, CalculatedValue g) {
        if (f.isComplex() || g.isComplex()) {
            return setComplexValue(f.getComplex().pow(g.getComplex()));
        } else {
            return setValue(FastMath.pow(f.real, g.real));
        }
    }

    public ValueType abs(CalculatedValue g) {
        return setValue(g.isComplex() ? FastMath.hypot(g.real, g.imaginary) : FastMath.abs(g.real));
    }

    public ValueType sqrt(CalculatedValue g) {
        if (g.isComplex() || (g.isReal() && g.real < 0)) {
            return setComplexValue(g.getComplex().sqrt());
        } else {
            return setValue(FastMath.sqrt(g.real));
        }
    }

    public ValueType sin(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().sin());
        } else {
            return setValue(FastMath.sin(g.real));
        }
    }

    public ValueType asin(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().asin());
        } else {
            return setValue(FastMath.asin(g.real));
        }
    }

    public ValueType sinh(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().sinh());
        } else {
            return setValue(FastMath.sinh(g.real));
        }
    }

    public ValueType cos(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().cos());
        } else {
            return setValue(FastMath.cos(g.real));
        }
    }

    public ValueType acos(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().acos());
        } else {
            return setValue(FastMath.acos(g.real));
        }
    }

    public ValueType cosh(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().cosh());
        } else {
            return setValue(FastMath.cosh(g.real));
        }
    }

    public ValueType tan(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().tan());
        } else {
            return setValue(FastMath.tan(g.real));
        }
    }

    public ValueType atan(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().atan());
        } else {
            return setValue(FastMath.atan(g.real));
        }
    }

    public ValueType tanh(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().tanh());
        } else {
            return setValue(FastMath.tanh(g.real));
        }
    }

    public ValueType exp(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().exp());
        } else {
            return setValue(FastMath.exp(g.real));
        }
    }

    public ValueType log(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().log());
        } else {
            return setValue(FastMath.log(g.real));
        }
    }

    public ValueType log10(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.getComplex().log().divide(FastMath.log(10.0)));
        } else {
            return setValue(FastMath.log10(g.real));
        }
    }

    public ValueType ceil(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(FastMath.ceil(g.real), FastMath.ceil(g.imaginary));
        } else {
            return setValue(FastMath.ceil(g.real));
        }
    }

    public ValueType floor(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(FastMath.floor(g.real), FastMath.floor(g.imaginary));
        } else {
            return setValue(FastMath.floor(g.real));
        }
    }

    public ValueType random(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(FastMath.random() * g.real, FastMath.random() * g.imaginary);
        } else {
            return setValue(FastMath.random() * g.real);
        }
    }

    public ValueType hypot(CalculatedValue f, CalculatedValue g) {
        if (f.isComplex() || g.isComplex()) {
            return setComplexValue(f.getComplex().pow(2.0).add(g.getComplex().pow(2.0)).sqrt());
        } else {
            return setValue(FastMath.hypot(f.real, g.real));
        }
    }

    public ValueType nthRoot(CalculatedValue g, int n) {
        try {
            final List<Complex> roots = g.getComplex().nthRoot(n);
            for (Complex root : roots) {
                if (FastMath.abs(root.getImaginary()) < 1E-15) {
                    return setValue(root.getReal());
                }
            }
            if (!roots.isEmpty()) {
                return setComplexValue(roots.get(0));
            }
        } catch (Exception ex) {
            // nothing to do
        }
        return invalidate(ErrorType.NOT_A_NUMBER);
    }

    public ValueType conj(CalculatedValue g) {
        if (g.isComplex()) {
            return setComplexValue(g.real, -1.0 * g.imaginary);
        } else {
            return setValue(g.real);
        }
    }

    public enum ErrorType {
        TERM_NOT_READY,
        INVALID_ARGUMENT,
        NOT_A_NUMBER,
        NOT_A_REAL,
        PASSED_COMPLEX
    }

    public enum ValueType {
        INVALID,
        REAL,
        COMPLEX
    }

    public enum PartType {
        RE,
        IM
    }
}
