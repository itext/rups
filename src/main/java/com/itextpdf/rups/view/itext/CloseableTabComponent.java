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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Tab component for a {@link JTabbedPane}, which adds a close tab button next
 * to the label. Button is based on the current Look & Feel.
 *
 * <p>
 * Button just fires an event and doesn't do anything on its own. Add a
 * listener to handle the button trigger.
 * </p>
 */
public final class CloseableTabComponent extends JPanel {
    private static final int SEPARATOR_WIDTH = 8;

    private final List<IntConsumer> closeButtonListeners = new ArrayList<>();

    public CloseableTabComponent(JTabbedPane parent) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        final JLabel label = new JLabel() {
            @Override
            public String getText() {
                final int idx = parent.indexOfTabComponent(CloseableTabComponent.this);
                if (idx < 0) {
                    return null;
                }
                return parent.getTitleAt(idx);
            }
        };
        label.setVerticalAlignment(SwingConstants.CENTER);

        final JButton button = new CloseButton();
        button.addActionListener((ActionEvent e) -> {
            final int idx = parent.indexOfTabComponent(this);
            if (idx >= 0) {
                for (final IntConsumer listener : closeButtonListeners) {
                    listener.accept(idx);
                }
            }
        });

        add(label);
        add(Box.createHorizontalStrut(SEPARATOR_WIDTH));
        add(button);
    }

    public void addCloseButtonListener(IntConsumer listener) {
        closeButtonListeners.add(Objects.requireNonNull(listener));
    }

    private static final class CloseButton extends JButton {
        public CloseButton() {
            super(UIManager.getIcon("InternalFrame.closeIcon"));

            setBorderPainted(false);
            setBorder(null);
            setFocusable(false);
            setMargin(new Insets(0, 0, 0, 0));
            setContentAreaFilled(false);
            setRolloverEnabled(true);
            setVerticalAlignment(SwingConstants.CENTER);
        }
    }
}
