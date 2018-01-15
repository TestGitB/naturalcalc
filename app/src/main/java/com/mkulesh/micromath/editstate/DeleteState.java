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
package com.mkulesh.micromath.editstate;

import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.type.BaseType;
import com.mkulesh.micromath.formula.views.FormulaView;

import java.util.ArrayList;

/*
 * Class that holds the undo state of multiply deletion.
 */
public final class DeleteState implements Parcelable {
    public static final Parcelable.Creator<DeleteState> CREATOR = new Parcelable.Creator<DeleteState>() {
        @Override
        public DeleteState createFromParcel(Parcel in) {
            return new DeleteState(in);
        }

        @Override
        public DeleteState[] newArray(int size) {
            return new DeleteState[size];
        }
    };
    private final ArrayList<EntryState> entries = new ArrayList<>();

    public DeleteState() {
        super();
    }

    public DeleteState(Parcel in) {
        super();
        in.readTypedList(entries, EntryState.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(entries);
    }

    public ArrayList<EntryState> getEntries() {
        return entries;
    }

    public void store(FormulaView f, Coordinate coor) {
        entries.add(0, new EntryState(f.getBaseType(), coor, f.onSaveInstanceState()));
    }

    /*
     * Helper class that holds the undo state of a single deletion.
     */
    public static class EntryState implements Parcelable {
        public static final Parcelable.Creator<EntryState> CREATOR = new Parcelable.Creator<EntryState>() {
            @Override
            public EntryState createFromParcel(Parcel in) {
                return new EntryState(in);
            }

            @Override
            public EntryState[] newArray(int size) {
                return new EntryState[size];
            }
        };
        public BaseType type;
        public Coordinate coordinate;
        public Parcelable data;

        public EntryState(BaseType type, Coordinate coordinate, Parcelable data) {
            super();
            this.type = type;
            this.coordinate = coordinate;
            this.data = data;
        }

        public EntryState(Parcel in) {
            super();
            type = BaseType.values()[in.readInt()];
            coordinate = in.readParcelable(Coordinate.class.getClassLoader());
            data = in.readParcelable(getClass().getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type.ordinal());
            dest.writeParcelable(coordinate, flags);
            dest.writeParcelable(data, flags);
        }
    }
}
