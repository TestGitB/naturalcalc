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

import com.mkulesh.micromath.utils.ViewUtils;

/**
 * Structure that holds row and column of a formula
 */
public final class Coordinate implements Parcelable {
    public static final Parcelable.Creator<Coordinate> CREATOR = new Parcelable.Creator<Coordinate>() {
        @Override
        public Coordinate createFromParcel(Parcel in) {
            return new Coordinate(in);
        }

        @Override
        public Coordinate[] newArray(int size) {
            return new Coordinate[size];
        }
    };
    public int row = ViewUtils.INVALID_INDEX;
    public int col = ViewUtils.INVALID_INDEX;

    public Coordinate() {
        super();
    }

    public Coordinate(int row, int col, boolean newRow) {
        super();
        this.row = row;
        this.col = col;
    }

    public Coordinate(Parcel in) {
        super();
        row = in.readInt();
        col = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(row);
        dest.writeInt(col);
    }
}
