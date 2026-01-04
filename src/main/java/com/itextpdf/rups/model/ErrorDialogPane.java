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

import java.awt.Dialog;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.SwingUtilities;

/**
 * A utility to display a dialog showing Throwable object
 */
// Excluding from coverage as it contains only UI code
@ExcludeFromGeneratedJacocoReport
public final class ErrorDialogPane {
    private static final int DEFAULT_DOUBLED_MARGIN = 100;
    private static final int FALLBACK_MAX_SIZE_DIM = 200;

    private ErrorDialogPane() {
        // do not instantiate
    }

    public static void showErrorDialog(Component parent, Throwable th) {
        final String msg = getTraceString(th);
        final JTextArea textArea = new JTextArea(msg);
        final JScrollPane scrollPane = new JScrollPane(textArea);
        /*
         * Update the dialog, created by JOptionPane, using the HierarchyListener.
         * Taken from https://stackoverflow.com/a/7989417/6564861 and the linked blog post
         * (which was cached in Wayback Machine).
         */
        scrollPane.addHierarchyListener(ErrorDialogPane::tweakDialogSize);
        JOptionPane.showMessageDialog(parent, scrollPane);
    }

    private static void tweakDialogSize(HierarchyEvent e) {
        final Window window = SwingUtilities.getWindowAncestor(e.getComponent());
        // We are fishing for JOptionPane dialog...
        if (!(window instanceof Dialog)) {
            return;
        }
        Dialog dialog = (Dialog) window;
        /*
         * If dialog was set to resizeable, then this handler was already
         * called for the dialog. So no reason to change sizes again.
         */
        if (dialog.isResizable()) {
            return;
        }
        dialog.setResizable(true);
        limitDialogSize(dialog);
    }

    /**
     * Limits the preferred size of the window based on the linked graphics
     * configuration.
     *
     * @param window Window to update.
     */
    private static void limitDialogSize(Window window) {
        final GraphicsConfiguration gc = window.getGraphicsConfiguration();
        if (gc == null) {
            return;
        }
        final Rectangle bounds = gc.getBounds();
        final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        final int gcMaxWidth = Math.max(
                FALLBACK_MAX_SIZE_DIM,
                bounds.width - insets.left - insets.right - DEFAULT_DOUBLED_MARGIN
        );
        final int gcMaxHeight = Math.max(
                FALLBACK_MAX_SIZE_DIM,
                bounds.height - insets.top - insets.bottom - DEFAULT_DOUBLED_MARGIN
        );
        final Dimension currentSize = window.getPreferredSize();
        boolean sizeModified = false;
        if (currentSize.width > gcMaxWidth) {
            currentSize.width = gcMaxWidth;
            sizeModified = true;
        }
        if (currentSize.height > gcMaxHeight) {
            currentSize.height = gcMaxHeight;
            sizeModified = true;
        }
        /*
         * We should call setPreferredSize only when we actually change the
         * size. As it might be calculated dynamically, if not set explicitly.
         */
        if (sizeModified) {
            window.setPreferredSize(currentSize);
        }
    }

    private static String getTraceString(Throwable th) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        final Optional<Throwable> chuckIt = Optional.ofNullable(th);
        chuckIt.map(Throwable::getLocalizedMessage)
                .ifPresent(pw::println);
        chuckIt.map(Throwable::getCause)
                .map(Throwable::getLocalizedMessage)
                .ifPresent((String msg) -> {
                    pw.print("Caused by: ");
                    pw.println(msg);
                });
        pw.append("[Stack Trace]\n");
        chuckIt.ifPresent(throwable -> throwable.printStackTrace(pw));
        return sw.toString();
    }
}
