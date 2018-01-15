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
package com.mkulesh.micromath.formula.io;

import android.os.AsyncTask;
import android.util.Xml;

import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.type.BaseType;
import com.mkulesh.micromath.formula.views.FormulaView;
import com.mkulesh.micromath.utils.SynchronizedBoolean;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.utils.XmlUtils;
import com.mkulesh.micromath.widgets.OnListChangeListener.Position;
import com.nstudio.calc.casio.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.Locale;

public class XmlLoaderTask extends AsyncTask<Void, BaseType, Void> {
    private final FormulaList list;
    private final InputStream stream;
    private final String name;
    private final SynchronizedBoolean isPublishRuns = new SynchronizedBoolean();
    private final SynchronizedBoolean isAborted = new SynchronizedBoolean();
    // result of operation
    public String error = null;
    public PostAction postAction = null;
    private XmlPullParser parser = null;
    private int firstFormulaId = ViewUtils.INVALID_INDEX;

    public XmlLoaderTask(FormulaList list, InputStream stream, String name, PostAction postAction) {
        this.list = list;
        this.stream = stream;
        this.name = name;
        this.postAction = postAction;
    }

    @Override
    protected void onPreExecute() {
        list.clearAll();
        list.setInOperation(this, true, null);
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        isPublishRuns.set(false);
        isAborted.set(false);
        try {
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            parser.nextTag();
            boolean isValidFormat = false;
            if (parser.getAttributeCount() > 0) {
                String prop = parser.getAttributeValue(0);
                if (prop != null && Constants.XML_HTTP.equals(prop)) {
                    isValidFormat = true;
                }
            }
            if (!isValidFormat) {
                error = String.format(list.getActivity().getResources().getString(R.string.error_unknown_file_format),
                        name);
                ViewUtils.debug(this, error + ": " + Constants.XML_PROP_MMT + " key is not found");
                return null;
            }
            parser.require(XmlPullParser.START_TAG, Constants.XML_NS, Constants.XML_MAIN_TAG);
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                final String n1 = parser.getName();
                if (n1.equals(Constants.XML_LIST_TAG)) {
                    parser.require(XmlPullParser.START_TAG, Constants.XML_NS, Constants.XML_LIST_TAG);
                    list.getDocumentSettings().readFromXml(parser);
                    while (true) {
                        if (parser.next() == XmlPullParser.END_TAG) {
                            break;
                        }
                        if (parser.getEventType() != XmlPullParser.START_TAG) {
                            continue;
                        }
                        final String n2 = parser.getName();
                        BaseType t = null;
                        try {
                            t = BaseType.valueOf(n2.toUpperCase(Locale.ENGLISH));
                        } catch (Exception ex) {
                            // nothing to do
                        }
                        if (t != null) {
                            isPublishRuns.set(true);
                            parser.require(XmlPullParser.START_TAG, Constants.XML_NS, n2);
                            publishProgress(t);
                            synchronized (isPublishRuns) {
                                while (isPublishRuns.isSet()) {
                                    isPublishRuns.wait();
                                }
                            }
                            if (error != null) {
                                return null;
                            }
                        } else {
                            XmlUtils.skipEntry(parser);
                        }
                        if (isAborted.isSet()) {
                            error = null;
                            postAction = PostAction.INTERRUPT;
                            return null;
                        }
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            // nothing to do
                        }
                    }
                } else {
                    XmlUtils.skipEntry(parser);
                }
            }
        } catch (Exception e) {
            error = String.format(list.getActivity().getResources().getString(R.string.error_file_read), name);
            ViewUtils.debug(this, error + ", " + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(BaseType... t) {
        if (isAborted.isSet()) {
            isPublishRuns.set(false);
            return;
        }
        FormulaView f = list.addBaseFormula(t[0]);
        try {
            f.readFromXml(parser);
        } catch (Exception e) {
            error = String.format(list.getActivity().getResources().getString(R.string.error_file_read), name);
            ViewUtils.debug(this, error + ", " + e.getLocalizedMessage());
        }
        if (f != null) {
            list.getFormulaListView().add(f, null, Position.AFTER); // add to the end
            if (firstFormulaId < 0) {
                firstFormulaId = f.getId();
            }
        }
        isPublishRuns.set(false);
    }

    @Override
    protected void onPostExecute(Void par) {
        FileUtils.closeStream(stream);
        if (list.getSelectedFormulaId() == ViewUtils.INVALID_INDEX) {
            list.setSelectedFormula(firstFormulaId, false);
        }
        list.setInOperation(this, false, null);
    }

    public void abort() {
        ViewUtils.debug(this, "trying to cancel XML loader task " + this.toString());
        isAborted.set(true);
    }

    public enum PostAction {
        NONE,
        CALCULATE,
        INTERRUPT
    }
}
