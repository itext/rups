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
package com.itextpdf.rups.view.itext.editor;

import java.awt.event.FocusEvent;
import org.fife.ui.rtextarea.ConfigurableCaret;

/**
 * Our custom {@link ConfigurableCaret}, which remains visible, if the text
 * area is not editable.
 */
public final class CustomConfigurableCaret extends ConfigurableCaret {
    private static final int DEFAULT_BLINK_RATE = 500;

    public CustomConfigurableCaret() {
        /*
         * The situation is a bit odd. Usually a caret is created via the UI
         * class, and then the blink rate is set manually in that class after
         * creation based on some component properties.
         *
         * But what it also means is that if you replace the caret in a text
         * area afterward, it will not blink, even though it is the default
         * behavior. So for simplicity we will set it here.
         */
        setBlinkRate(DEFAULT_BLINK_RATE);
    }

    @Override
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        if (getComponent().isEnabled()) {
            setVisible(true);
        }
    }
}
