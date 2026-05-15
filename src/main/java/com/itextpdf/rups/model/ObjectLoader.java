/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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
package com.itextpdf.rups.model;

import com.itextpdf.rups.util.ExcludeFromGeneratedJacocoReport;
import com.itextpdf.rups.view.Language;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Loads the necessary iText PDF objects in Background.
 */
public final class ObjectLoader extends SwingWorker<Void, Void> {
    /**
     * This is the object that wait for task to complete.
     */
    private final IRupsEventListener eventListener;
    /**
     * RUPS's PdfFile object.
     */
    private final IPdfFile file;
    /**
     * The factory that can provide PDF objects.
     */
    private IndirectObjectFactory objects;
    /**
     * The factory that can provide tree nodes.
     */
    private TreeNodeFactory nodes;
    /**
     * a human readable name for this loaded
     */
    private final String loaderName;

    private final IProgressDialog progress;

    /**
     * Creates a new ObjectLoader.
     *
     * @param loaderName the loader name
     * @param progress   the progress dialog
     * @param eventListener   the object that will forward the changes.
     * @param file       the PdfFile from which the objects will be read.
     */
    public ObjectLoader(IRupsEventListener eventListener, IPdfFile file, String loaderName, IProgressDialog progress) {
        this.eventListener = eventListener;
        this.file = file;
        this.loaderName = loaderName;
        this.progress = progress;
    }

    /**
     * Getter for the PdfReader object.
     *
     * @return a reader object
     */
    public IPdfFile getFile() {
        return file;
    }

    /**
     * Getter for the object factory.
     *
     * @return an indirect object factory
     */
    public IndirectObjectFactory getObjects() {
        return objects;
    }

    /**
     * Getter for the tree node factory.
     *
     * @return a tree node factory
     */
    public TreeNodeFactory getNodes() {
        return nodes;
    }


    /**
     * getter for a human readable name representing this loader
     *
     * @return the human readable name
     * @since 5.0.3
     */
    public String getLoaderName() {
        return loaderName;
    }

    @Override
    protected Void doInBackground() {
        clearResult();
        objects = new IndirectObjectFactory(file.getPdfDocument());
        final int n = objects.getXRefMaximum();
        SwingUtilities.invokeLater(() -> {
            progress.setMessage(Language.XREF_READING.getString());
            progress.setTotal(n);
        });
        while (objects.storeNextObject()) {
            SwingUtilities.invokeLater(() -> progress.setValue(objects.getCurrent()));
        }
        SwingUtilities.invokeLater(() -> progress.setTotal(0));
        nodes = new TreeNodeFactory(objects);
        SwingUtilities.invokeLater(() -> progress.setMessage(Language.GUI_UPDATING.getString()));
        return null;
    }

    @Override
    protected void done() {
        if (handleTaskException()) {
            return;
        }
        try {
            eventListener.handleOpenDocument(this);
            SwingUtilities.invokeLater(this::hideProgress);
        } catch (RuntimeException ex) {
            displayThrowable(ex);
        }
    }

    // Excluding from coverage as all exceptions scenarios involve showing
    // a modal dialog, which is not easy to test in a headless env...
    @ExcludeFromGeneratedJacocoReport
    private boolean handleTaskException() {
        try {
            // This will throw an ExecutionException, if doInBackground
            // threw an exception
            get(0L, TimeUnit.NANOSECONDS);
            return false;
        } catch (ExecutionException e) {
            displayThrowable(e.getCause());
            clearResult();
        } catch (TimeoutException | RuntimeException e) {
            // This should not happen in practice
            displayThrowable(e);
            clearResult();
        } catch (InterruptedException e) {
            // This should not happen in practice
            displayThrowable(e);
            clearResult();
            Thread.currentThread().interrupt();
        }
        return true;
    }

    private void clearResult() {
        objects = null;
        nodes = null;
    }

    // Excluding from coverage as effects are only shown in the UI
    @ExcludeFromGeneratedJacocoReport
    private void displayThrowable(Throwable th) {
        LoggerHelper.error(th.getLocalizedMessage(), th, getClass());
        progress.showErrorDialog(th);
        SwingUtilities.invokeLater(this::hideProgress);
    }

    private void hideProgress() {
        progress.setVisible(false);
    }
}
