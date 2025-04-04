/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
    Authors: Apryse Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    APRYSE GROUP. APRYSE GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups.view.itext;

import com.itextpdf.rups.model.IPdfFile;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.view.Language;

import java.io.UnsupportedEncodingException;
import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;

public final class PlainText extends ReadOnlyTextArea implements IRupsEventListener {

    private boolean loaded = false;

    private IPdfFile file = null;

    private SwingWorker<String, Object> worker = null;

    public PlainText() {
        // Empty
    }

    public void openPlainText() {
        if (file == null || loaded) {
            return;
        }
        loaded = true;
        setText(Language.LOADING.getString());
        worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return getFileContentAsString(file);
            }

            @Override
            protected void done() {
                if (!isCancelled()) {
                    String text;
                    try {
                        text = get();
                    } catch (InterruptedException any) {
                        text = Language.ERROR_WHILE_LOADING_TEXT.getString();
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException any) {
                        text = Language.ERROR_WHILE_LOADING_TEXT.getString();
                    }
                    setText(text);
                }
            }
        };
        worker.execute();
    }

    private static String getFileContentAsString(IPdfFile file) {
        try {
            return new String(file.getOriginalContent(), "cp1252");
        } catch (UnsupportedEncodingException e) {
            return Language.ERROR_WRONG_ENCODING.getString();
        }
    }

    @Override
    public void handleCloseDocument() {
        file = null;
        setText("");
        if (worker != null) {
            worker.cancel(true);
            worker = null;
        }
        loaded = false;
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        file = loader.getFile();
        loaded = false;
        if (worker != null) {
            worker.cancel(true);
            worker = null;
        }
    }
}
