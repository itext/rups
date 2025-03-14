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
package com.itextpdf.rups.view.itext.treenodes.asn1;

import com.itextpdf.rups.view.JsonPrettyPrinter;
import com.itextpdf.rups.view.contextmenu.IPdfContextMenuTarget;
import com.itextpdf.rups.view.icons.IconTreeNode;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactory.Feature;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamWriteFeature;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * Base tree node for showing ASN.1 primitives.
 */
public abstract class AbstractAsn1TreeNode
        extends IconTreeNode
        implements IPdfContextMenuTarget, Iterable<AbstractAsn1TreeNode> {
    /**
     * Name of the field, represented by the node, as it is specified in the
     * corresponding RFC document.
     */
    private String rfcFieldName;
    /**
     * Optional ASN.1 value explanation string. For example, this might be a
     * human-friendly string for an OID, or an enumeration value name.
     */
    private String valueExplanation;

    /**
     * Creates a new tree node with the specified icon, which holds the object.
     *
     * @param icon   Icon to use for the tree node.
     * @param object ASN.1 primitive, which the node represents.
     */
    protected AbstractAsn1TreeNode(String icon, ASN1Primitive object) {
        super(getIconPath(icon), object);
    }

    /**
     * Returns the name of the field, represented by the node, as it is
     * specified in the corresponding RFC document.
     *
     * @return String with the name or null, if unknown.
     */
    public String getRfcFieldName() {
        return rfcFieldName;
    }

    /**
     * Sets the name of the field, represented by the node, as it is
     * specified in the corresponding RFC document.
     *
     * @param rfcFieldName String with the RFC name.
     */
    public void setRfcFieldName(String rfcFieldName) {
        this.rfcFieldName = rfcFieldName;
    }

    /**
     * Returns the optional ASN.1 value explanation string.
     *
     * @return String with the explanation or null, if missing.
     */
    public String getValueExplanation() {
        return valueExplanation;
    }

    /**
     * Sets the optional ASN.1 value explanation string.
     *
     * @param valueExplanation String with the explanation.
     */
    public void setValueExplanation(String valueExplanation) {
        this.valueExplanation = valueExplanation;
    }

    /**
     * Returns the display string for the ASN.1 primitive type.
     *
     * @return String with the ASN.1 primitive type.
     */
    public abstract String getAsn1Type();

    /**
     * Returns the display string for the ASN.1 primitive type, including the
     * tag information.
     *
     * @return String with the ASN.1 primitive type and the tag number.
     */
    public String getAsn1FullType() {
        final String tag = getAsn1DisplayTag();
        if (tag == null) {
            return getAsn1Type();
        }
        return tag + " " + getAsn1Type();
    }

    /**
     * Returns the context-specific tag display string for the ASN.1 primitive
     * type.
     *
     * @return String with the context-specific tag. Null, if the primitive has
     * a universal tag.
     */
    public String getAsn1DisplayTag() {
        return null;
    }

    /**
     * Returns the display string for the ASN.1 primitive value.
     *
     * @return String with the ASN.1 primitive value.
     */
    public String getAsn1DisplayValue() {
        return getAsn1Primitive().toString();
    }

    /**
     * Returns the ASN.1 primitive behind the node.
     *
     * @return The ASN.1 primitive behind the node.
     */
    public ASN1Primitive getAsn1Primitive() {
        return (ASN1Primitive) getUserObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractAsn1TreeNode getChildAt(int index) {
        return (AbstractAsn1TreeNode) super.getChildAt(index);
    }

    /**
     * Returns the string representation of the tree node.
     *
     * @return The string representation of the tree node.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        final String rfc = getRfcFieldName();
        if (rfc != null) {
            sb.append(rfc);
        } else {
            sb.append(getAsn1FullType());
        }

        final String value = getAsn1DisplayValue();
        if (value != null) {
            sb.append(": ").append(value);
            final String explanation = getValueExplanation();
            if (explanation != null) {
                sb.append(" (").append(explanation).append(')');
            }
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsInspectObject() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsSave() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<AbstractAsn1TreeNode> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < getChildCount();
            }

            @Override
            public AbstractAsn1TreeNode next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final AbstractAsn1TreeNode child = getChildAt(index);
                ++index;
                return child;
            }
        };
    }

    /**
     * Updates the tree node to match the data in the underlying ASN.1 object.
     */
    public void reload() {
        // If value changes, then value explanation should also change
        setValueExplanation(null);
    }

    /**
     * Writes the tree JSON representation to the output stream as a
     * human-readable UTF-8 string.
     *
     * @param output Stream to write the string to.
     *
     * @throws IOException If there is either an underlying I/O problem or
     *                     encoding issue at format layer.
     */
    public void toDisplayJson(OutputStream output) throws IOException {
        final JsonFactory factory = JsonFactory.builder()
                /*
                 * We don't really need DoS protection in this case, so
                 * disabling it to prevent having random exceptions
                 * thrown.
                 */
                .disable(Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW)
                /*
                 * Caller will be responsible for closing the stream. We only
                 * append here.
                 */
                .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
                .build();
        try (final JsonGenerator jsonGenerator = factory.createGenerator(output)) {
            jsonGenerator.setPrettyPrinter(new JsonPrettyPrinter());
            toDisplayJsonInternal(jsonGenerator);
        }
    }

    /**
     * Writes the tree JSON representation via the JSON generator.
     *
     * @param json JSON generator to use for output.
     *
     * @throws IOException If there is either an underlying I/O problem or
     *                     encoding issue at format layer.
     */
    private void toDisplayJsonInternal(JsonGenerator json) throws IOException {
        json.writeStartObject();
        writeStringFieldIfNotNull(json, "name", getRfcFieldName());
        writeStringFieldIfNotNull(json, "type", getAsn1FullType());
        writeStringFieldIfNotNull(json, "value", getAsn1DisplayValue());
        writeStringFieldIfNotNull(json, "explanation", getValueExplanation());
        if (getChildCount() > 0) {
            json.writeFieldName("children");
            json.writeStartArray();
            for (final AbstractAsn1TreeNode child : this) {
                child.toDisplayJsonInternal(json);
            }
            json.writeEndArray();
        }
        json.writeEndObject();
    }

    /**
     * Convenience method for outputting a field entry ("member")
     * that has a String value. If value is null, nothing is written.
     *
     * @param json  JsonGenerator object to call methods on.
     * @param key   Name of the field to write.
     * @param value String value of the field to write.
     *
     * @throws IOException if there is either an underlying I/O problem or encoding
     *                     issue at format layer.
     */
    private static void writeStringFieldIfNotNull(JsonGenerator json, String key, String value)
            throws IOException {
        if (value != null) {
            json.writeStringField(key, value);
        }
    }

    /**
     * Returns the path in resources to the ASN.1 icon.
     *
     * @param iconBaseName ASN.1 icon file basename
     *
     * @return The path in resources to the ASN.1 icon.
     */
    private static String getIconPath(String iconBaseName) {
        if (iconBaseName == null) {
            return null;
        }
        return "asn1/" + iconBaseName;
    }
}
