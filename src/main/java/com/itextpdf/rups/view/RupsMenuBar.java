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
package com.itextpdf.rups.view;

import com.itextpdf.kernel.actions.data.ITextCoreProductData;
import com.itextpdf.rups.RupsConfiguration;
import com.itextpdf.rups.controller.IRupsController;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.io.OpenInViewerAction;
import com.itextpdf.rups.io.PdfFileOpenAction;
import com.itextpdf.rups.io.PdfFileSaveAction;
import com.itextpdf.rups.model.IPdfFile;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.model.MruListHandler;
import com.itextpdf.rups.model.ObjectLoader;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class RupsMenuBar extends JMenuBar implements IRupsEventListener {
    private final IRupsController controller;
    /**
     * The action needed to open a file.
     */
    private final PdfFileOpenAction fileOpenAction;
    /**
     * The Preferences Window
     */
    private final PreferencesWindow preferencesWindow;

    private final JMenuItem reopenAsOwnerMenuItem;
    private final JMenuItem closeMenuItem;
    private final JMenuItem saveAsMenuItem;
    private final JMenuItem openInPdfViewerMenuItem;

    /**
     * Creates a JMenuBar.
     */
    public RupsMenuBar(RupsController controller) {
        this.controller = controller;

        preferencesWindow = new PreferencesWindow();

        fileOpenAction = new PdfFileOpenAction(controller::openNewFile, controller.getMasterComponent());

        final JMenu file = new JMenu(Language.MENU_BAR_FILE.getString());
        addItem(
                file,
                Language.MENU_BAR_OPEN,
                fileOpenAction,
                KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK)
        );
        file.add(createOpenRecentSubMenu());
        reopenAsOwnerMenuItem = addItem(
                file,
                Language.MENU_BAR_REOPEN_AS_OWNER,
                e -> controller.reopenAsOwner()
        );
        closeMenuItem = addItem(
                file,
                Language.MENU_BAR_CLOSE,
                e -> controller.closeCurrentFile(),
                KeyStroke.getKeyStroke('W', InputEvent.CTRL_DOWN_MASK)
        );
        saveAsMenuItem = addItem(
                file,
                Language.MENU_BAR_SAVE_AS,
                new PdfFileSaveAction(controller, controller.getMasterComponent()),
                KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK)
        );
        file.addSeparator();
        openInPdfViewerMenuItem = addItem(
                file,
                Language.MENU_BAR_OPEN_IN_PDF_VIEWER,
                new OpenInViewerAction(controller),
                KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)
        );
        add(file);

        final JMenu edit = new JMenu(Language.MENU_BAR_EDIT.getString());
        addItem(
                edit,
                Language.PREFERENCES,
                e -> preferencesWindow.show(controller.getMasterComponent())
        );
        add(edit);

        add(Box.createGlue());

        final JMenu help = new JMenu(Language.MENU_BAR_HELP.getString());
        addItem(
                help,
                Language.MENU_BAR_ABOUT,
                new MessageAction(Language.MESSAGE_ABOUT.getString())
        );
        addItem(
                help,
                Language.MENU_BAR_VERSION,
                new MessageAction(ITextCoreProductData.getInstance().getVersion())
        );
        add(help);
        onDisplayedFileChanged(controller);

        controller.addPdfFileOpenListener(f -> onDisplayedFileChanged(controller));
        controller.addRupsEventListener(this);
    }

    @Override
    public void handleAllFilesClosed() {
        onDisplayedFileChanged(controller);
    }

    @Override
    public void handleDisplayedTabChanged(IPdfFile file) {
        onDisplayedFileChanged(controller);
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        onDisplayedFileChanged(controller);
    }

    private void onDisplayedFileChanged(IRupsController controller) {
        final IPdfFile currentFile = controller.getCurrentFile();
        // "Reopen As Owner" makes sense only if the selected file was opened
        // in a restricted mode
        reopenAsOwnerMenuItem.setEnabled(currentFile != null && !currentFile.isOpenedAsOwner());
        // "Close" should be enabled, when there is a "closeable" tab present
        closeMenuItem.setEnabled(!controller.isDefaultTabShown());
        // "Save As" should be enabled only if there is an "editable" file
        // currently selected
        saveAsMenuItem.setEnabled(currentFile != null && currentFile.isOpenedAsOwner());
        // "Open In PDF Viewer" should be enabled for any opened file
        openInPdfViewerMenuItem.setEnabled(currentFile != null);
    }

    private JMenu createOpenRecentSubMenu() {
        final MruListHandler mruListHandler = RupsConfiguration.INSTANCE.getMruListHandler();
        final JMenu menu = new JMenu(Language.MENU_BAR_OPEN_RECENT.getString());
        populateOpenRecentSubMenu(menu, mruListHandler);
        mruListHandler.addChangeListener(mru -> populateOpenRecentSubMenu(menu, mru));
        return menu;
    }

    private void populateOpenRecentSubMenu(JMenu menu, MruListHandler mru) {
        menu.removeAll();

        // There is no reason to open an empty menu...
        menu.setEnabled(!mru.isEmpty());
        for (int i = mru.size() - 1; i >= 0; --i) {
            final File mruElement = mru.peek(i);
            addItem(menu, mruElement.getAbsolutePath(), e -> controller.openNewFile(mruElement));
        }
        if (!mru.isEmpty()) {
            menu.addSeparator();
            addItem(menu, Language.MENU_BAR_CLEAR_RECENTLY_OPENED, e -> mru.clear());
        }
    }

    /**
     * Create an item with a certain caption and a certain action,
     * then add the item to a menu.
     *
     * @param menu    the menu to which the item has to be added
     * @param caption the caption of the item
     * @param action  the action corresponding with the caption
     */
    private static JMenuItem addItem(JMenu menu, Language caption, ActionListener action) {
        return addItem(menu, caption.getString(), action, null);
    }

    private static JMenuItem addItem(JMenu menu, Language caption, ActionListener action, KeyStroke keyStroke) {
        return addItem(menu, caption.getString(), action, keyStroke);
    }

    private static JMenuItem addItem(JMenu menu, String caption, ActionListener action) {
        return addItem(menu, caption, action, null);
    }

    private static JMenuItem addItem(JMenu menu, String caption, ActionListener action, KeyStroke keyStroke) {
        final JMenuItem item = new JMenuItem(caption);
        item.addActionListener(action);
        if (keyStroke != null) {
            item.setAccelerator(keyStroke);
        }
        menu.add(item);
        return item;
    }
}
