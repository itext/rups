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
package com.itextpdf.rups.view.itext.stream;

import com.itextpdf.rups.controller.PdfReaderController;
import com.itextpdf.rups.model.IRupsEventListener;
import com.itextpdf.rups.model.ObjectLoader;
import com.itextpdf.rups.model.PdfStreamUtil;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;

import java.awt.CardLayout;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Pane for showing PDF stream content.
 *
 * <p>
 * For images the pane shows the image itself, with the relevant image options.
 * </p>
 *
 * <p>
 * For everything else (at the moment of writing) a syntax editor is used.
 * </p>
 */
public final class StreamPane extends JPanel implements IRupsEventListener {
    private final StreamTextEditorPane textEditorPane;
    private final StreamImagePane imagePane;
    private final JPanel emptyPane;

    public StreamPane(PdfReaderController controller) {
        this.textEditorPane = new StreamTextEditorPane(controller);
        this.textEditorPane.setVisible(false);
        this.imagePane = new StreamImagePane();
        this.imagePane.setVisible(false);
        this.emptyPane = new JPanel();
        this.emptyPane.setVisible(true);

        setLayout(new CardLayout());
        add(this.textEditorPane);
        add(this.imagePane);
        add(this.emptyPane);
    }

    public void render(PdfObjectTreeNode target) {
        if (target == null || !target.isStream()) {
            showPane(emptyPane);
            return;
        }
        final BufferedImage image = PdfStreamUtil.getAsImage(target.getAsStream());
        if (image != null) {
            imagePane.setImage(image);
            showPane(imagePane);
            return;
        }
        textEditorPane.render(target);
        showPane(textEditorPane);
    }

    @Override
    public void handleCloseDocument() {
        showPane(emptyPane);
        textEditorPane.handleCloseDocument();
    }

    @Override
    public void handleOpenDocument(ObjectLoader loader) {
        showPane(emptyPane);
        textEditorPane.handleOpenDocument(loader);
    }

    private void showPane(JComponent pane) {
        assert pane != null;

        showImagePane(imagePane == pane);
        showTextEditorPane(textEditorPane == pane);
        emptyPane.setVisible(emptyPane == pane);
        validate();
    }

    private void showImagePane(boolean flag) {
        imagePane.setVisible(flag);
        if (!flag) {
            imagePane.setImage(null);
        }
    }

    private void showTextEditorPane(boolean flag) {
        textEditorPane.setVisible(flag);
        if (!flag) {
            textEditorPane.render(null);
        }
    }
}
