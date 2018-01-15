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
package com.mkulesh.micromath.formula.button;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import com.mkulesh.micromath.utils.ViewUtils;
import com.nstudio.calc.casio.R;

import java.util.Arrays;

public class CalcImageButton extends AppCompatImageView implements ICalcButton {
    private final boolean[] enabled = new boolean[Category.values().length];
    private String code = null;
    private String shortCut = null;
    private Category[] categories = null;

    public CalcImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        enableAll();
        setup(context);
    }

    public CalcImageButton(Context context) {
        super(context);
        enableAll();
        setup(context);

    }

    public CalcImageButton(Context context, int shortCutId, int descriptionId, String code) {
        super(context);
        setup(context);
        initWithParameter(shortCutId, descriptionId, code);
    }

    public void initWithParameter(int shortCutId, int descriptionId, String code) {
        if (shortCutId != CalcButtonManager.NO_BUTTON) {
            shortCut = getContext().getResources().getString(shortCutId);
        }
        if (descriptionId != CalcButtonManager.NO_BUTTON) {
            String description = getContext().getResources().getString(descriptionId);
            if (shortCut != null) {
                description += " ('";
                description += shortCut;
                description += "')";
            }
            setContentDescription(description);
            setLongClickable(true);
        }
        this.code = code;
        enableAll();
        ViewUtils.setImageButtonColorAttr(getContext(), this,
                isEnabled() ? R.attr.colorMicroMathIcon : R.attr.colorPrimaryDark);
    }

    private void setup(Context context) {
        final int buttonSize = context.getResources().getDimensionPixelSize(R.dimen.activity_toolbar_height);
        setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        setBackgroundResource(outValue.resourceId);
    }

    public String getCategoryCode() {
        return code;
    }

    public String getShortCut() {
        return shortCut;
    }

    public Category[] getCategories() {
        return categories;
    }

    public void setCategories(Category[] categories) {
        this.categories = categories;
    }

    private void enableAll() {
        Arrays.fill(enabled, true);
    }

    public void setEnabled(Category t, boolean value) {
        enabled[t.ordinal()] = value;
        super.setEnabled(true);
        for (boolean en : enabled) {
            if (!en) {
                super.setEnabled(false);
                break;
            }
        }
    }


}