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
package com.mkulesh.micromath.formula;

import com.mkulesh.micromath.formula.views.TermField;
import com.mkulesh.micromath.formula.views.EquationView;
import com.mkulesh.micromath.formula.views.FormulaView;
import com.mkulesh.micromath.math.CalculatedValue;
import com.nstudio.calc.casio.R;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CalcEditText;

import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;

public class TermParser {
    public static final String CONST_NAN = "NaN";
    public static final String CONST_INF = "∞";
    public static final String CONST_E = "e";
    public static final String CONST_PI1 = "π";
    public static final String CONST_PI2 = "pi";
    public static final String IMAGINARY_UNIT = "i";
    public static final String POSITIVE_SIGN = "+";
    public static final String NEGATIVE_SIGN = "-";

    public int errorId = TermField.NO_ERROR_ID;

    private CalculatedValue value = null;
    private String functionName = null;
    private ArrayList<String> functionArgs = null;
    private IArgumentHolder argumentHolder = null;
    private int argumentIndex = ViewUtils.INVALID_INDEX, linkedVariableId = -1;
    private double sign = 1.0;
    private boolean isArray = false;

    public static Complex complexValueOf(String text) {
        // text shall contain imaginary unit
        if (text == null || !text.contains(IMAGINARY_UNIT)) {
            return null;
        }

        // imaginary unit shall be the last character
        final int unitPos = text.indexOf(IMAGINARY_UNIT);
        if (unitPos != text.length() - 1) {
            return null;
        }

        // search for +/- sign before imaginary unit
        int signPos = text.lastIndexOf(POSITIVE_SIGN);
        if (signPos < 0) {
            signPos = text.lastIndexOf(NEGATIVE_SIGN);
        }
        if (signPos < 0) {
            signPos = 0;
        }

        // split real and imaginary part
        String rePart = "", imPart = "";
        try {
            rePart = (signPos > 0) ? text.substring(0, signPos) : "0.0";
            imPart = (unitPos > signPos) ? text.substring(signPos, unitPos) : "1.0";
            if (imPart.equals(POSITIVE_SIGN) || imPart.equals(NEGATIVE_SIGN)) {
                imPart += "1.0";
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        // convert both parts
        try {
            return new Complex(Double.valueOf(rePart), Double.valueOf(imPart));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public CalculatedValue getValue() {
        return value;
    }

    public String getFunctionName() {
        return functionName;
    }

    public ArrayList<String> getFunctionArgs() {
        return functionArgs;
    }

    public IArgumentHolder getArgumentHolder() {
        return argumentHolder;
    }

    public int getArgumentIndex() {
        return argumentIndex;
    }

    public int getLinkedVariableId() {
        return linkedVariableId;
    }

    public double getSign() {
        return sign;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setText(TermField owner, FormulaView formulaRoot, CalcEditText editText) {
        String inText = editText.getText().toString();
        value = null;
        functionName = null;
        functionArgs = null;
        argumentHolder = null;
        argumentIndex = ViewUtils.INVALID_INDEX;
        linkedVariableId = -1;
        sign = 1.0;
        isArray = false;
        errorId = TermField.NO_ERROR_ID;
        if (inText == null || inText.length() == 0) {
            return;
        }

        String text = inText.trim();

        // check for forbidden content
        if (CONST_NAN.equals(text) || CONST_INF.equals(text)) {
            errorId = R.string.error_nan_value;
            return;
        }
        if ((editText.isIndexName() || editText.isEquationName()) && IMAGINARY_UNIT.equalsIgnoreCase(text)) {
            errorId = R.string.error_forbidden_imaginary_unit;
            return;
        }

        // check if is a valid double value
        try {
            value = new CalculatedValue(CalculatedValue.ValueType.REAL, Double.parseDouble(text), 0.0);
            return;
        } catch (Exception ex) {
            value = null;
            // nothing to do: we will try to convert it to the function name
        }

        // check if is a valid complex value
        try {
            Complex cmplValue = complexValueOf(text);
            if (cmplValue != null) {
                if (!editText.isComplexEnabled()) {
                    errorId = R.string.error_forbidden_complex;
                    return;
                }
                if (cmplValue.getImaginary() != 0.0) {
                    value = new CalculatedValue(CalculatedValue.ValueType.COMPLEX, cmplValue.getReal(),
                            cmplValue.getImaginary());
                } else {
                    value = new CalculatedValue(CalculatedValue.ValueType.REAL, cmplValue.getReal(), 0.0);
                }
                return;
            }
        } catch (Exception ex) {
            value = null;
            // nothing to do: we will try to convert it to the function name
        }

        // check for the sign
        if (text.startsWith("-")) {
            sign = -1.0;
            text = text.substring(1).trim();
        }

        // check if it is a constant
        if (CONST_E.equalsIgnoreCase(text)) {
            value = new CalculatedValue(CalculatedValue.ValueType.REAL, sign * Math.E, 0.0);
            sign = +1.0;
            return;
        } else if (CONST_PI1.equalsIgnoreCase(text) || CONST_PI2.equalsIgnoreCase(text)) {
            value = new CalculatedValue(CalculatedValue.ValueType.REAL, sign * Math.PI, 0.0);
            sign = +1.0;
            return;
        }

        // check if it is a function name
        BracketParser brPars = new BracketParser();
        switch (brPars.parse(text, formulaRoot.getContext().getResources())) {
            case NO_BRACKETS:
                functionName = text;
                break;
            case PARSED_SUCCESSFULLY:
                functionName = brPars.name;
                functionArgs = brPars.arguments;
                isArray = brPars.isArray();
                break;
            case PARSED_WITH_ERROR:
                errorId = brPars.errorId;
                return;
        }

        if (functionName != null) {
            // check for argument index
            if (checkArgumentIndex(owner, editText)) {
                // found a valid argument
                return;
            } else if (errorId != TermField.NO_ERROR_ID) {
                // found an erroneous argument
                return;
            }

            // check for index name
            if (editText.isIndexName() && functionArgs != null) {
                // error: found a field that contains argument but it shall be a index
                errorId = R.string.error_forbidden_arguments;
                return;
            }

            // check for equation name
            if (editText.isEquationName()) {
                final FormulaView fb = formulaRoot.getFormulaList().getFormula(functionName, ViewUtils.INVALID_INDEX,
                        formulaRoot.getId(), true);
                if (fb != null && !formulaRoot.getFormulaList().getDocumentSettings().redefineAllowed) {
                    // error: we found an other equation with the same name as this equation definition:
                    // it is forbidden
                    errorId = R.string.error_duplicated_identifier;
                } else {
                    // this equation definition contains a valid and unique name
                }
                return;
            }

            // check the link to a variable
            final FormulaView fb = formulaRoot.getFormulaList().getFormula(functionName, 0, formulaRoot.getId(), true);
            if (fb != null && fb instanceof EquationView) {
                ArrayList<String> args = ((EquationView) fb).getArguments();
                if (args == null || args.isEmpty()) {
                    linkedVariableId = fb.getId();
                    if (linkedVariableId >= 0) {
                        // we found a link to the valid constant
                        return;
                    }
                }
            }
        }

        errorId = R.string.error_unknown_variable;
    }

    private boolean checkArgumentIndex(TermField owner, CalcEditText editText) {
        argumentHolder = owner.findArgumentHolder(functionName);
        // no argument holder is found
        if (argumentHolder == null || !(argumentHolder instanceof FormulaView)) {
            return false;
        }
        if (editText.isEquationName()) {
            // get the parent holder in the case of an equation name
            final TermField parentHolderTerm = ((FormulaView) argumentHolder).getParentField();
            argumentHolder = (parentHolderTerm != null) ? parentHolderTerm.findArgumentHolder(functionName) : null;
            if (argumentHolder != null) {
                errorId = R.string.error_duplicated_identifier;
            }
            argumentHolder = null;
            return false;
        }
        if (editText.isIntermediateArgument()) {
            // get the parent holder in the case of an intermediate argument
            final TermField parentHolderTerm = ((FormulaView) argumentHolder).getParentField();
            argumentHolder = (parentHolderTerm != null) ? parentHolderTerm.findArgumentHolder(functionName) : null;
            if (argumentHolder == null) {
                return false;
            }
        }
        // obtain argument index
        argumentIndex = argumentHolder.getArgumentIndex(functionName);
        if (argumentIndex == ViewUtils.INVALID_INDEX) {
            // should never happen since findArgumentHolder already checks the argument index
            argumentHolder = null;
        }
        return (argumentHolder != null && argumentIndex != ViewUtils.INVALID_INDEX);
    }

    public boolean isArgumentInHolder(String var) {
        if (argumentHolder != null) {
            final ArrayList<String> args = argumentHolder.getArguments();
            if (args != null && getArgumentIndex() >= 0 && getArgumentIndex() < args.size()) {
                final String arg = args.get(getArgumentIndex());
                if (var != null && arg != null) {
                    return var.equals(arg);
                }
            }
        }
        return false;
    }
}
