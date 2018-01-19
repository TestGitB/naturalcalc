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

package com.mkulesh.micromath.widgets;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.IdGenerator;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class ResultMatrixLayout extends TableLayout {
    private final ArrayList<FormulaEditText> fields = new ArrayList<>();
    private int rowsNumber = 0;
    private int colsNumber = 0;


    public ResultMatrixLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResultMatrixLayout(Context context) {
        super(context);
    }

    @Override
    public int getBaseline() {
        int height = getPaddingTop();
        for (int row = 0; row < getChildCount(); row++) {
            final View child = getChildAt(row);
            height += child.getMeasuredHeight();
        }
        height += getPaddingBottom();
        return height / 2;
    }

    public void resize(int rows, int cols, int cellLayoutId) {
        if (rowsNumber == rows && colsNumber == cols) {
            return;
        }
        rowsNumber = rows;
        colsNumber = cols;

        removeAllViews();
        fields.clear();

        final TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        for (int row = 0; row < rowsNumber; row++) {
            final TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(tableParams); // TableLayout is the parent view
            addView(tableRow);

            for (int col = 0; col < colsNumber; col++) {
                inflater.inflate(cellLayoutId, tableRow);
            }

            if (tableRow.getChildCount() > 0) {
                tableRow.setBaselineAligned(true);
                tableRow.setBaselineAlignedChildIndex(0);
            }

            for (int col = 0; col < tableRow.getChildCount(); col++) {
                final FormulaEditText c = (FormulaEditText) tableRow.getChildAt(col);
                if (c != null) {
                    c.setId(IdGenerator.generateId());
                    c.setTag(new ElementTag(row, col, fields.size()));
                    fields.add(c);
                }
            }
        }

        setPadding(0, 0, 0, 0);
        setBaselineAligned(true);
        setBaselineAlignedChildIndex(rowsNumber > 1 ? rowsNumber / 2 : 0);
    }

    private FormulaEditText getCell(int row, int col) {
        if (row < getChildCount()) {
            final TableRow tr = (TableRow) getChildAt(row);
            if (tr != null && col < tr.getChildCount()) {
                return (FormulaEditText) tr.getChildAt(col);
            }
        }
        return null;
    }

    public void setText(int row, int col, String text) {
        final FormulaEditText cell = getCell(row, col);
        if (cell != null) {
            cell.setText(text);
        }
    }

    public void updateTextSize(ScaledDimensions dimen) {
        for (FormulaEditText field : fields) {
            field.updateTextSize(dimen, 0, ScaledDimensions.Type.MATRIX_COLUMN_PADDING);
        }
    }

    public void prepare(AppCompatActivity activity, OnFormulaChangeListener termChangeIf, OnFocusChangedListener onFocusChangedListener) {
        for (FormulaEditText field : fields) {
            field.setup(activity, termChangeIf);
            field.setChangeListener(null, onFocusChangedListener);
        }
    }

    public void updateTextColor(int normalDrawable, int selectedDrawable, @AttrRes int colorAttr) {
        for (FormulaEditText field : fields) {
            if (field.isSelected()) {
                CompatUtils.updateBackgroundAttr(getContext(), field, selectedDrawable, colorAttr);
            } else {
                CompatUtils.updateBackground(getContext(), field, normalDrawable);
            }
        }
    }

    public boolean isCell(FormulaEditText c) {
        return c != null && c.getTag() != null && c.getTag() instanceof ElementTag;
    }

    public int getFirstFocusId() {
        return fields.isEmpty() ? ViewUtils.INVALID_INDEX : fields.get(0).getId();
    }

    public int getLastFocusId() {
        return fields.isEmpty() ? ViewUtils.INVALID_INDEX : fields.get(fields.size() - 1).getId();
    }

    public int getNextFocusId(FormulaEditText c, OnFocusChangedListener.FocusType focusType) {
        if (!isCell(c)) {
            return ViewUtils.INVALID_INDEX;
        }
        ElementTag tag = (ElementTag) c.getTag();
        FormulaEditText nextC = null;
        switch (focusType) {
            case FOCUS_DOWN:
                nextC = tag.row + 1 < rowsNumber ? getCell(tag.row + 1, tag.col) : null;
                break;
            case FOCUS_LEFT:
                nextC = tag.index >= 1 ? fields.get(tag.index - 1) : null;
                break;
            case FOCUS_RIGHT:
                nextC = tag.index + 1 < fields.size() ? fields.get(tag.index + 1) : null;
                break;
            case FOCUS_UP:
                nextC = tag.row >= 1 ? getCell(tag.row - 1, tag.col) : null;
                break;
        }
        return nextC == null ? ViewUtils.INVALID_INDEX : nextC.getId();
    }

    public void setText(String s, ScaledDimensions dimen) {
        for (FormulaEditText field : fields) {
            field.setText(s);
            field.updateTextSize(dimen, 0, ScaledDimensions.Type.MATRIX_COLUMN_PADDING);
        }
    }

    public final class ElementTag {
        public final int row;
        public final int col;
        public final int index;

        public ElementTag(int row, int col, int index) {
            this.row = row;
            this.col = col;
            this.index = index;
        }
    }
}
