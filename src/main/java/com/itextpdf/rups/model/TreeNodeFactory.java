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
package com.itextpdf.rups.model;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.rups.view.Language;
import com.itextpdf.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.itextpdf.rups.view.itext.treenodes.PdfPagesTreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.CertificateCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.ContentInfoCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.AbstractCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.CrlCorrector;
import com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509.OcspResponseCorrector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.TreeNode;

/**
 * A factory that creates TreeNode objects corresponding with PDF objects.
 */
public class TreeNodeFactory {
    // These should be available in later versions of iText, remove later
    private static final PdfName PdfNameIssuer = new PdfName("Issuer");
    private static final PdfName PdfNameSV = new PdfName("SV");
    private static final PdfName PdfNameSVCert = new PdfName("SVCert");
    private static final PdfName PdfNameTS = new PdfName("TS");

    /**
     * The factory that can produce all indirect objects.
     */
    protected IndirectObjectFactory objects;
    /**
     * An list containing the nodes of every indirect object.
     */
    private final List<PdfObjectTreeNode> nodes = new ArrayList<>();

    /**
     * Creates a factory that can produce TreeNode objects
     * corresponding with PDF objects.
     *
     * @param objects a factory that can produce all the indirect objects of a PDF file.
     */
    public TreeNodeFactory(IndirectObjectFactory objects) {
        this.objects = objects;
        for (int i = 0; i < objects.size(); i++) {
            final int ref = objects.getRefByIndex(i);
            nodes.add(PdfObjectTreeNode.getInstance(PdfNull.PDF_NULL, ref));
        }
    }

    /**
     * Gets a TreeNode for an indirect objects.
     *
     * @param ref the reference number of the indirect object.
     *
     * @return the TreeNode representing the PDF object
     */
    public PdfObjectTreeNode getNode(int ref) {
        int idx = objects.getIndexByRef(ref);
        PdfObjectTreeNode node = nodes.get(idx);
        if (node.getPdfObject().isNull()) {
            node = PdfObjectTreeNode.getInstance(objects.loadObjectByReference(ref), ref);
            nodes.set(idx, node);
        }
        return node;
    }

    protected void associateIfIndirect(PdfObjectTreeNode node) {
        PdfIndirectReference ref = null;
        if (node != null && node.getPdfObject() != null) {
            ref = node.getPdfObject().getIndirectReference();
        }
        if (ref != null) {
            final int idx = objects.getIndexByRef(ref.getObjNumber());
            nodes.set(idx, node);
        }
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode.
     *
     * @param node the parent node
     */
    public void expandNode(PdfObjectTreeNode node) {
        if (node.getChildCount() > 0) {
            return;
        }

        final PdfObject object = node.getPdfObject();
        PdfObjectTreeNode leaf;
        switch (object.getType()) {
            case PdfObject.INDIRECT_REFERENCE:
                final PdfIndirectReference ref = (PdfIndirectReference) object;
                leaf = getNode(ref.getObjNumber());
                addNodes(node, leaf);
                if (leaf instanceof PdfPagesTreeNode) {
                    expandNode(leaf);
                }
                break;
            case PdfObject.ARRAY:
                final PdfArray array = (PdfArray) object;
                for (int i = 0; i < array.size(); ++i) {
                    leaf = PdfObjectTreeNode.getInstance(array.get(i, false));
                    associateIfIndirect(leaf);
                    addNodes(node, leaf);
                    expandNode(leaf);
                }
                break;
            case PdfObject.DICTIONARY:
            case PdfObject.STREAM:
                final PdfDictionary dict = (PdfDictionary) object;
                for (PdfName key : dict.keySet()) {
                    leaf = PdfObjectTreeNode.getInstance(dict, key);
                    associateIfIndirect(leaf);
                    addNodes(node, leaf);
                    expandNode(leaf);
                }
                break;
        }

        // Additional handling for ASN.1 stuff
        expandAsn1Nodes(node);
    }

    /**
     * Finds a specific child of dictionary node.
     * This method will follow indirect references and expand nodes if necessary
     *
     * @param node the node with a dictionary among its children
     * @param key  the key of the item corresponding with the node we need
     *
     * @return a specific child of dictionary node
     */
    @SuppressWarnings("unchecked")
    public PdfObjectTreeNode getChildNode(PdfObjectTreeNode node, PdfName key) {
        PdfObjectTreeNode child = node.getDictionaryChildNode(key);
        if (child != null && child.isDictionaryNode(key)) {
            if (child.isIndirectReference()) {
                expandNode(child);
                child = (PdfObjectTreeNode) child.getFirstChild();
            }
            expandNode(child);
            return child;
        }
        return null;
    }

    /**
     * Tries adding a child node to a parent node without
     * throwing an exception. Normally, if the child node is already
     * added as one of the ancestors, an IllegalArgumentException is
     * thrown (to avoid an endless loop). Loops like this are allowed
     * in PDF, not in a JTree.
     *
     * @param parent the parent node
     * @param child  a child node
     */
    private void addNodes(PdfObjectTreeNode parent, PdfObjectTreeNode child) {
        try {
            parent.add(child);
        } catch (IllegalArgumentException iae) {
            parent.setRecursive(true);
        }
    }

    public void addNewIndirectObject(PdfObject object) {
        objects.addNewIndirectObject(object);
        nodes.add(PdfObjectTreeNode.getInstance(object, object.getIndirectReference().getObjNumber()));
        LoggerHelper.info(Language.LOG_TREE_NODE_CREATED.getString(), getClass());
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode, which
     * contains DER-encoded ASN.1 bytes.
     *
     * @param node the parent node.
     *
     * @return true if this is the correct node, regardless of whether a child
     * was added.
     */
    private static boolean expandAsn1Nodes(PdfObjectTreeNode node) {
        return expandSigContentsNode(node)
                || expandSvCertArrayValueNode(node)
                || expandDssDataNode(node);
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode, which
     * is a /Contents entry in a /Sig or /DocTimeStamp dictionary.
     *
     * @param node the parent node.
     *
     * @return true if this is the correct node, regardless of whether a child
     * was added.
     */
    private static boolean expandSigContentsNode(PdfObjectTreeNode node) {
        // This should be a string under the /Contents key
        if (!node.isDictionaryNode(PdfName.Contents) || !node.getPdfObject().isString()) {
            return false;
        }

        final PdfObjectTreeNode parent = getDirectParentNode(node);
        if (parent == null) {
            return false;
        }

        // Parent should be a /Sig or a /DocTimeStamp dictionary
        if (!isSigDict(parent)) {
            return false;
        }

        /*
         * At this point we assume, that the PDF string contains an encoded
         * ASN.1 object inside. This might not be true for custom signature
         * themes, but it seems like a reasonable default to use.
         */
        final PdfString nodeObject = (PdfString) node.getPdfObject();
        final AbstractAsn1TreeNode asn1 = Asn1TreeNodeFactory.fromPrimitive(nodeObject.getValueBytes());
        if (asn1 != null) {
            /*
             * While this corrector only covers the CMS case, we can reasonably
             * assume, that it is correct to use it. The only case in the
             * standard, when this is not correct, is adbe.x509.rsa_sha1. But
             * in that case the corrector will just do nothing, as it is just
             * an octet string.
             */
            ContentInfoCorrector.INSTANCE.correct(asn1);
            node.add(asn1);
        } else {
            LoggerHelper.warnf(
                    Language.WARNING_FAILED_TO_PARSE_AS_ASN1_OBJECT,
                    TreeNodeFactory.class,
                    PdfName.Sig,
                    PdfName.Contents
            );
        }
        return true;
    }

    /**
     * Checks, whether this is a dictionary, which corresponds to the value of
     * a /Sig form field.
     *
     * @param node the dictionary node to check.
     *
     * @return true if this is the correct node.
     */
    private static boolean isSigDict(PdfObjectTreeNode node) {
        /*
         * If there is a type field, and it is one of the valid values, then we
         * can answer this immediately.
         */
        final PdfName dictType = node.getPdfDictionaryType();
        if (PdfName.Sig.equals(dictType) || PdfName.DocTimeStamp.equals(dictType)) {
            return true;
        }
        /*
         * In this case there is a type, but it is not what we expected... So
         * we can safely assume it is not a signature.
         */
        if (dictType != null) {
            return false;
        }
        /*
         * In case the type field is missing, then we will check the form field
         * the dictionary is supposed to be in as a fallback. It should be a
         * form field dictionary with the form field type of /Sig and the node
         * object should be the value of that field. Either directly or
         * indirectly.
         */
        return isUnderSigFormFieldDict(node, PdfName.V);
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode, which
     * is inside /Subject or /Issuer arrays in a /SVCert dictionary.
     *
     * @param node the parent node.
     *
     * @return true if this is the correct node, regardless of whether a child
     * was added.
     */
    private static boolean expandSvCertArrayValueNode(PdfObjectTreeNode node) {
        // This should be a string
        if (!node.getPdfObject().isString()) {
            return false;
        }

        final PdfObjectTreeNode parent = getDirectParentNode(node);
        if (parent == null) {
            return false;
        }

        // Parent should be under one of the supported keys
        if (!parent.isDictionaryNode(PdfName.Subject) && !parent.isDictionaryNode(PdfNameIssuer)) {
            return false;
        }

        final PdfObjectTreeNode grandparent = getDirectParentNode(parent);
        if (grandparent == null) {
            return false;
        }

        // Grandparent should be a /SVCert dictionary
        if (!isCertificateSeedValueNode(grandparent)) {
            return false;
        }

        // By standard, it should be a DER-encoded certificate
        final PdfString nodeObject = (PdfString) node.getPdfObject();
        final AbstractAsn1TreeNode asn1 = Asn1TreeNodeFactory.fromPrimitive(nodeObject.getValueBytes());
        if (asn1 != null) {
            CertificateCorrector.INSTANCE.correct(asn1);
            node.add(asn1);
        } else {
            LoggerHelper.warnf(
                    Language.WARNING_FAILED_TO_PARSE_AS_ASN1_OBJECT,
                    TreeNodeFactory.class,
                    "/SVCert",
                    parent.isDictionaryNode(PdfName.Subject) ? PdfName.Subject : "/Issuer"
            );
        }
        return true;
    }

    /**
     * Checks, whether this is a certificate seed value dictionary.
     *
     * @param node the dictionary node to check.
     *
     * @return true if this is the correct node.
     */
    private static boolean isCertificateSeedValueNode(PdfObjectTreeNode node) {
        /*
         * If there is a type field, and it is one of the valid values, then we
         * can answer this immediately.
         */
        PdfName dictType = node.getPdfDictionaryType();
        if (PdfNameSVCert.equals(dictType)) {
            return true;
        }
        /*
         * In this case there is a type, but it is not what we expected... So
         * we can safely assume it is not a certificate seed value.
         */
        if (dictType != null) {
            return false;
        }
        /*
         * In case the type field is missing, then we will check its parent.
         * It should be a seed value dictionary and the current node should be
         * under /Cert either directly or indirectly.
         */
        final PdfObjectTreeNode parent = getDirectParentNodeUnderKey(node, PdfName.Cert);
        if (parent == null) {
            return false;
        }
        dictType = parent.getPdfDictionaryType();
        if (PdfNameSV.equals(dictType)) {
            return true;
        }
        if (dictType != null) {
            return false;
        }
        /*
         * In case the type field in the parent is also missing, then we will
         * check the form field the parent is supposed to be in as a fallback.
         * It should be a form field dictionary with the form field type of
         * /Sig and the parent object should be under /SV there.
         */
        return isUnderSigFormFieldDict(parent, PdfNameSV);
    }

    /**
     * Checks, whether this node is inside a /Sig for field dictionary under
     * the specified key.
     *
     * @param node the dictionary node to check.
     *
     * @return true if node is inside the signature form field under key.
     */
    private static boolean isUnderSigFormFieldDict(PdfObjectTreeNode node, PdfName key) {
        final PdfObjectTreeNode parent = getDirectParentNodeUnderKey(node, key);
        if (parent == null || !parent.isDictionary()) {
            return false;
        }
        return PdfName.Sig.equals(((PdfDictionary) parent.getPdfObject()).get(PdfName.FT));
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode, which
     * is a DER-encoded object under the DSS dictionary nodes.
     *
     * @param node the parent node.
     *
     * @return true if this is the correct node, regardless of whether a child
     * was added.
     */
    private static boolean expandDssDataNode(PdfObjectTreeNode node) {
        return expandDssArrayNode(node, List.of(PdfName.Cert, PdfName.Certs), CertificateCorrector.INSTANCE)
                || expandDssArrayNode(node, List.of(PdfName.CRL, PdfName.CRLs), CrlCorrector.INSTANCE)
                || expandDssArrayNode(node, List.of(PdfName.OCSP, PdfName.OCSPs), OcspResponseCorrector.INSTANCE)
                || expandDssTsNode(node);
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode, which
     * is in an array under DSS.
     *
     * @param node the parent node.
     * @param keys Keys, under which the array should be.
     * @param corrector Corrector to use for the node.
     *
     * @return true if this is the correct node, regardless of whether a child
     * was added.
     */
    private static boolean expandDssArrayNode(
            PdfObjectTreeNode node,
            Iterable<PdfName> keys,
            AbstractCorrector corrector
    ) {
        // This should be a stream
        if (!node.isStream()) {
            return false;
        }

        /*
         * Parent should an array, which is under one of the provided keys.
         *
         * We could check the dictionary types and more, but names seem to be
         * pretty self-explanatory, so it should work fine.
         */
        final PdfObjectTreeNode parent = getDirectParentNode(node);
        if (parent == null || !parent.isArray()) {
            return false;
        }
        final PdfObjectTreeNode grandparent = getDirectParentNodeUnderKeys(parent, keys);
        if (grandparent == null) {
            return false;
        }

        final PdfStream nodeObject = (PdfStream) node.getPdfObject();
        final AbstractAsn1TreeNode asn1 = Asn1TreeNodeFactory.fromPrimitive(nodeObject.getBytes());
        if (asn1 != null) {
            corrector.correct(asn1);
            node.add(asn1);
        } else {
            LoggerHelper.warnf(
                    Language.WARNING_FAILED_TO_PARSE_AS_ASN1_OBJECT,
                    TreeNodeFactory.class,
                    PdfName.DSS,
                    keys.iterator().next()
            );
        }
        return true;
    }

    /**
     * Creates the Child TreeNode objects for a PDF object TreeNode, which
     * is a stream under /TS in DSS.
     *
     * @param node the parent node.
     *
     * @return true if this is the correct node, regardless of whether a child
     * was added.
     */
    private static boolean expandDssTsNode(PdfObjectTreeNode node) {
        // This should be a stream
        if (!node.isStream()) {
            return false;
        }

        /*
         * Node should be a stream under a /TS key.
         *
         * We could check the dictionary types and more, but names seem to be
         * pretty self-explanatory, so it should work fine.
         */
        if (getDirectParentNodeUnderKey(node, PdfNameTS) == null) {
            return false;
        }

        final PdfStream nodeObject = (PdfStream) node.getPdfObject();
        final AbstractAsn1TreeNode asn1 = Asn1TreeNodeFactory.fromPrimitive(nodeObject.getBytes());
        if (asn1 != null) {
            ContentInfoCorrector.INSTANCE.correct(asn1);
            node.add(asn1);
        } else {
            LoggerHelper.warnf(
                    Language.WARNING_FAILED_TO_PARSE_AS_ASN1_OBJECT,
                    TreeNodeFactory.class,
                    PdfName.DSS,
                    PdfNameTS
            );
        }
        return true;
    }

    /**
     * Returns the first parent node, which is not an indirect reference node.
     *
     * @param node Node to get the parent for.
     *
     * @return The non-indirect reference parent node.
     */
    private static PdfObjectTreeNode getDirectParentNode(PdfObjectTreeNode node) {
        PdfObjectTreeNode child = node;
        while (true) {
            final TreeNode parent = child.getParent();
            if (!(parent instanceof PdfObjectTreeNode)) {
                return null;
            }
            final PdfObjectTreeNode pdfParent = (PdfObjectTreeNode) parent;
            if (!pdfParent.isIndirectReference()) {
                return pdfParent;
            }
            child = pdfParent;
        }
    }

    /**
     * Returns the first parent node, which is not an indirect reference node,
     * if we got there under the provided key.
     *
     * @param node Node to get the parent for.
     * @param key  Key, that we should arrive to the parent under.
     *
     * @return The non-indirect reference parent node, if node is under key.
     */
    private static PdfObjectTreeNode getDirectParentNodeUnderKey(
            PdfObjectTreeNode node,
            PdfName key
    ) {
        return getDirectParentNodeUnderKeys(node, Collections.singleton(key));
    }

    /**
     * Returns the first parent node, which is not an indirect reference node,
     * if we got there under one of the provided keys.
     *
     * @param node Node to get the parent for.
     * @param keys Keys, that we should arrive to the parent under.
     *
     * @return The non-indirect reference parent node, if node is under one of
     * the keys.
     */
    private static PdfObjectTreeNode getDirectParentNodeUnderKeys(
            PdfObjectTreeNode node,
            Iterable<PdfName> keys
    ) {
        PdfObjectTreeNode child = node;
        while (true) {
            final TreeNode parent = child.getParent();
            if (!(parent instanceof PdfObjectTreeNode)) {
                return null;
            }
            final PdfObjectTreeNode pdfParent = (PdfObjectTreeNode) parent;
            if (!pdfParent.isIndirectReference()) {
                if (child.isDictionaryNode(keys)) {
                    return pdfParent;
                }
                return null;
            }
            child = pdfParent;
        }
    }
}
