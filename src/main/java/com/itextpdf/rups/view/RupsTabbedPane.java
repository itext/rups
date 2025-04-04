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
package com.itextpdf.rups.view;

import com.itextpdf.rups.Rups;
import com.itextpdf.rups.controller.RupsInstanceController;
import com.itextpdf.rups.model.IPdfFile;
import com.itextpdf.rups.view.itext.CloseableTabComponent;
import com.itextpdf.rups.view.itext.ITabClosedListener;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The class holding the JTabbedPane that holds the Rups tabs. This class is responsible for loading, closing, and
 * saving PDF files handled by the {@link com.itextpdf.rups.controller.RupsInstanceController rupsInstanceController}
 * object that is tied to a {@link com.itextpdf.rups.view.RupsPanel RupsPanel}, which is the main content pane for the
 * tabs in the JTabbedPane.
 */
public class RupsTabbedPane {

    private final JPanel defaultTab;
    private final JTabbedPane jTabbedPane;
    private final List<ITabClosedListener> tabClosedListeners = new ArrayList<>();

    public RupsTabbedPane() {
        this.jTabbedPane = new JTabbedPane();
        this.defaultTab = new JPanel();
        this.defaultTab.add(new JLabel(Language.DEFAULT_TAB_TEXT.getString()));
        ensureDefaultTab();
    }

    public void addTabClosedListener(ITabClosedListener listener) {
        tabClosedListeners.add(Objects.requireNonNull(listener));
    }

    public void openNewFile(File file, Dimension dimension) {
        openNewFile(file, dimension, false);
    }

    public void openNewFile(File file, Dimension dimension, boolean requireEditable) {
        if (file != null) {
            if (isDefaultTabShown()) {
                this.jTabbedPane.removeTabAt(this.jTabbedPane.getSelectedIndex());
            }

            final RupsPanel rupsPanel = new RupsPanel();
            final RupsInstanceController rupsInstanceController =
                    new RupsInstanceController(dimension, rupsPanel);
            rupsPanel.setRupsInstanceController(rupsInstanceController);
            rupsInstanceController.loadFile(file, requireEditable);
            addTab(file.getName(), rupsPanel);
            this.jTabbedPane.setSelectedComponent(rupsPanel);
            showReadOnlyWarning();
        }
    }

    public boolean closeCurrentFile() {
        final Component comp = this.jTabbedPane.getSelectedComponent();
        if (comp instanceof RupsPanel) {
            this.jTabbedPane.removeTabAt(this.jTabbedPane.getSelectedIndex());
            ensureDefaultTab();
            fireTabClosed(((RupsPanel) comp).getPdfFile());
        }
        return isDefaultTabShown();
    }

    public void closeFile(IPdfFile file) {
        for (int i = 0; i < this.jTabbedPane.getTabCount(); i++) {
            final Component comp = this.jTabbedPane.getComponentAt(i);
            if ((comp instanceof RupsPanel) && ((RupsPanel) comp).getPdfFile() == file) {
                this.jTabbedPane.removeTabAt(i);
                ensureDefaultTab();
                fireTabClosed(file);
                return;
            }
        }
    }

    public IPdfFile getFile(int index) {
        final Component component = this.jTabbedPane.getComponentAt(index);
        if (component instanceof RupsPanel) {
            return ((RupsPanel) component).getPdfFile();
        }
        return null;
    }

    public IPdfFile getCurrentFile() {
        final int idx = this.jTabbedPane.getSelectedIndex();
        if (idx < 0) {
            return null;
        }
        return getFile(idx);
    }

    public void saveCurrentFile(File file) {
        final RupsPanel currentRupsPanel = (RupsPanel) this.jTabbedPane.getSelectedComponent();
        currentRupsPanel.getRupsInstanceController().saveFile(file);
    }

    public Component getJTabbedPane() {
        return this.jTabbedPane;
    }

    public boolean isDefaultTabShown() {
        return this.defaultTab == this.jTabbedPane.getSelectedComponent();
    }

    /**
     * Checks to see whether the provided file has already been opened in RUPS.
     *
     * @param file potentially duplicate file
     *
     * @return true if already opened, false if not
     */
    public boolean isFileAlreadyOpen(File file) {
        for (int tabIndex = 0; tabIndex < this.jTabbedPane.getTabCount(); tabIndex++) {
            final Component component = this.jTabbedPane.getComponentAt(tabIndex);

            if (component instanceof RupsPanel) {
                final RupsPanel rupsPanel = (RupsPanel) component;
                final IPdfFile pdfFile = rupsPanel.getPdfFile();
                if (pdfFile != null && pdfFile.getOriginalFile().equals(file)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addChangeListener(ChangeListener l) {
        this.jTabbedPane.addChangeListener(e -> l.stateChanged(new ChangeEvent(this)));
    }

    private void showReadOnlyWarning() {
        final IPdfFile currentFile = getCurrentFile();
        if (currentFile != null && !currentFile.isOpenedAsOwner()) {
            Rups.showBriefMessage(Language.WARNING_OPENED_IN_READ_ONLY_MODE.getString());
        }
    }

    private void addTab(String title, Component component) {
        final int index = this.jTabbedPane.getTabCount();
        this.jTabbedPane.addTab(title, component);
        final CloseableTabComponent tabComponent = new CloseableTabComponent(this.jTabbedPane);
        tabComponent.addCloseButtonListener(this::onTabCloseButtonClicked);
        this.jTabbedPane.setTabComponentAt(index, tabComponent);
    }

    private void onTabCloseButtonClicked(int i) {
        final IPdfFile file = getFile(i);
        this.jTabbedPane.removeTabAt(i);
        ensureDefaultTab();
        if (file != null) {
            fireTabClosed(file);
        }
    }

    private void ensureDefaultTab() {
        if (this.jTabbedPane.getTabCount() == 0) {
            // We don't call addTab class method for the default tab, as you
            // cannot close it, so there is no need for a close button.
            this.jTabbedPane.addTab(Language.DEFAULT_TAB_TITLE.getString(), this.defaultTab);
        }
    }

    private void fireTabClosed(IPdfFile file) {
        for (final ITabClosedListener listener: tabClosedListeners) {
            listener.onTabClosed(file, isDefaultTabShown());
        }
    }
}
