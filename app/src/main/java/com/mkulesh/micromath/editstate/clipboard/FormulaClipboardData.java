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
package com.mkulesh.micromath.editstate.clipboard;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.type.BaseType;

import java.util.ArrayList;

public class FormulaClipboardData {
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_CONTENT_TYPE = "contentType";
    private static final String STATE_DATA = "data";
    private ContentType contentType;
    private StoredTerm[] data;


    public FormulaClipboardData() {
    }

    public FormulaClipboardData(BaseType baseType, Parcelable data) {
        this.contentType = ContentType.FORMULA;
        this.data = new StoredTerm[1];
        this.data[0] = new StoredTerm(baseType, "", data);
    }

    public FormulaClipboardData(BaseType baseType, String termCode, Parcelable data) {
        this.contentType = ContentType.FORMULA;
        this.data = new StoredTerm[1];
        this.data[0] = new StoredTerm(baseType, termCode, data);
    }

    public FormulaClipboardData(ArrayList<BaseType> types, ArrayList<Parcelable> data) {
        this.contentType = ContentType.LIST;
        this.data = new StoredTerm[data.size()];
        for (int i = 0; i < data.size(); i++) {
            this.data[i] = new StoredTerm(types.get(i), "", data.get(i));
        }
    }

    /**
     * Parcelable interface: procedure writes the formula state
     */
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putString(STATE_CONTENT_TYPE, contentType.toString());
        bundle.putParcelableArray(STATE_DATA, data);
        return bundle;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null) {
            return;
        }
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            contentType = ContentType.valueOf(bundle.getString(STATE_CONTENT_TYPE));
            data = (StoredTerm[]) bundle.getParcelableArray(STATE_DATA);
        }
    }

    public ContentType getContentType() {
        return contentType;
    }

    public StoredTerm[] getArrayData() {
        return data;
    }

    public StoredTerm getSingleData() {
        return (data != null && data.length > 0) ? data[0] : null;
    }

    public enum ContentType {
        FORMULA,
        LIST
    }

    /*
     * Helper class that holds the clipboard state of a single formula.
     */
    public static class StoredTerm implements Parcelable {
        public static final Parcelable.Creator<StoredTerm> CREATOR = new Parcelable.Creator<StoredTerm>() {
            public StoredTerm createFromParcel(Parcel in) {
                return new StoredTerm(in);
            }

            public StoredTerm[] newArray(int size) {
                return new StoredTerm[size];
            }
        };
        public BaseType baseType;
        public String termCode;
        public Parcelable data;

        public StoredTerm(Parcel in) {
            super();
            readFromParcel(in);
        }

        public StoredTerm(BaseType baseType, String termCode, Parcelable data) {
            super();
            this.baseType = baseType;
            this.termCode = termCode;
            this.data = data;
        }

        public BaseType getBaseType() {
            return baseType;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(baseType.toString());
            dest.writeString(termCode);
            dest.writeParcelable(data, 0);
        }

        public void readFromParcel(Parcel in) {
            baseType = BaseType.valueOf(in.readString());
            termCode = in.readString();
            data = in.readParcelable(getClass().getClassLoader());
        }
    }
}
