/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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
package com.itextpdf.rups.controller;

import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.rups.model.IPdfFile;
import com.itextpdf.rups.model.LoggerHelper;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.RupsTabbedPane;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.event.ChangeEvent;

/**
 * This class controls all the GUI components that are shown in
 * the RUPS application: the menu bar, the panels,...
 */
public class RupsController implements IRupsController {

    private final RupsTabbedPane rupsTabbedPane;

    private final Dimension dimension;

    private final List<Consumer<File>> pdfFileOpenListeners = new ArrayList<>();
    private final List<IRupsEventListener> rupsEventListeners = new ArrayList<>();

    /**
     * Constructs the GUI components of the RUPS application.
     *
     * @param dimension      The dimension of the screen
     * @param rupsTabbedPane The main tabbed pane
     */
    public RupsController(Dimension dimension, RupsTabbedPane rupsTabbedPane) {
        this.rupsTabbedPane = rupsTabbedPane;
        this.rupsTabbedPane.addChangeListener(this::onTabChanged);
        this.rupsTabbedPane.addTabClosedListener(this::onTabClosed);

        this.dimension = new Dimension(dimension);
    }

    /**
     * Getter for the master component.
     *
     * @return the master component
     */
    @Override
    public Component getMasterComponent() {
        return rupsTabbedPane.getJTabbedPane();
    }

    @Override
    public void handleCloseDocument() {
        fireRupsEvent(IRupsEventListener::handleCloseDocument);
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        fireRupsEvent(c -> c.handleOpenDocument(loader));
    }

    @Override
    public void handleAllFilesClosed() {
        fireRupsEvent(IRupsEventListener::handleAllFilesClosed);
    }

    @Override
    public void handleDisplayedTabChanged(IPdfFile file) {
        fireRupsEvent(c -> c.handleDisplayedTabChanged(file));
    }

    @Override
    public void handleNewIndirectObject(PdfObject object) {
        fireRupsEvent(c -> c.handleNewIndirectObject(object));
    }

    @Override
    public final void closeCurrentFile() {
        this.rupsTabbedPane.closeCurrentFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCurrentFile(File newFile) {
        this.rupsTabbedPane.saveCurrentFile(newFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reopenAsOwner() {
        final IPdfFile file = this.getCurrentFile();
        // Should not happen (i.e. UI should not allow it), will just noop
        if (file == null) {
            LoggerHelper.warn(Language.ERROR_NO_OPEN_DOCUMENT.getString(), getClass());
            return;
        }

        openNewFileAsOwner(file.getOriginalFile());
        // Close the read-only file only if opening as owner was successful
        if (getCurrentFile() != null) {
            closeFile(file);
        }
    }

    @Override
    public final IPdfFile getCurrentFile() {
        return this.rupsTabbedPane.getCurrentFile();
    }

    @Override
    public boolean isDefaultTabShown() {
        return this.rupsTabbedPane.isDefaultTabShown();
    }

    @Override
    public final void openNewFile(File file) {
        if (file != null) {
            if (!RupsConfiguration.INSTANCE.canOpenDuplicateFiles() && this.rupsTabbedPane.isFileAlreadyOpen(file)) {
                LoggerHelper.info(Language.DUPLICATE_FILES_OFF.getString(), this.getClass());
                return;
            }

            this.rupsTabbedPane.openNewFile(file, this.dimension);
            firePdfFileOpen(file);
        }
    }

    public void createNewIndirectObject() {
        // TODO: This functionality was removed at some point, so this is just a noop placeholder
    }

    public void addPdfFileOpenListener(Consumer<File> listener) {
        pdfFileOpenListeners.add(Objects.requireNonNull(listener));
    }

    private void firePdfFileOpen(File file) {
        for (final Consumer<File> listener: pdfFileOpenListeners) {
            listener.accept(file);
        }
    }

    public void addRupsEventListener(IRupsEventListener listener) {
        rupsEventListeners.add(Objects.requireNonNull(listener));
    }

    private void fireRupsEvent(Consumer<IRupsEventListener> func) {
        for (final IRupsEventListener listener: rupsEventListeners) {
            func.accept(listener);
        }
    }

    private void openNewFileAsOwner(File file) {
        Objects.requireNonNull(file);

        // We don't check here for duplicate files, as we don't want to close
        // the already opened file, if we fail here

        this.rupsTabbedPane.openNewFile(file, this.dimension, true);
        firePdfFileOpen(file);
    }

    private void closeFile(IPdfFile file) {
        this.rupsTabbedPane.closeFile(file);
    }

    private void onTabChanged(ChangeEvent e) {
        fireRupsEvent(c -> c.handleDisplayedTabChanged(getCurrentFile()));
    }

    private void onTabClosed(IPdfFile file, boolean isLastTab) {
        if (isLastTab) {
            fireRupsEvent(IRupsEventListener::handleAllFilesClosed);
        }
    }
}
