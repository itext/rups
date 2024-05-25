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
package com.itextpdf.rups.view.itext.treenodes.asn1.correctors.x509;

import com.itextpdf.rups.view.itext.treenodes.asn1.AbstractAsn1TreeNode;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TestUtil;
import com.itextpdf.rups.view.itext.treenodes.asn1.Asn1TreeNodeFactory;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
final class BasicOcspResponseCorrectorTest {
    @Test
    void correct_WithDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        validateDefaultNode(node, "basicOCSPResponse");
    }

    @Test
    void correct_WithoutDefaultName() {
        final AbstractAsn1TreeNode node = createDefaultNode();
        BasicOcspResponseCorrector.INSTANCE.correct(node, "BOP");
        validateDefaultNode(node, "BOP");
    }

    @Test
    void correct_WithoutCerts() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(),
                        new DERSequence(),
                        new DERBitString((byte) 0xFF, 0),
                })
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(3, "basicOCSPResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "tbsResponseData", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "signatureAlgorithm", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "signature: 0b11111111", node.getChildAt(2));
    }

    @Test
    void correct_WithoutOptionalResponseFields() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 2,
                                        new DEROctetString(new byte[] {0x12, 0x23})
                                ),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(),
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID: 0x1223", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            Asn1TestUtil.assertNodeMatches(0, "responses", tbsResponseData.getChildAt(2));
        }
    }

    @Test
    void correct_NameResponderId() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                // No need to be more precise here...
                                new DERTaggedObject(true, 1, new DERSequence()),
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode responseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "tbsResponseData", responseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", responseData.getChildAt(0));
        }
    }

    @Test
    void correct_WithoutNextUpdate() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 2,
                                        new DEROctetString(new byte[] {0x12, 0x23})
                                ),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(),
                                                // good
                                                new DERTaggedObject(false, 0, DERNull.INSTANCE),
                                                new DERGeneralizedTime("20240401100000Z"),
                                                new DERTaggedObject(true, 1, new DERSequence()),
                                        })
                                ),
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID: 0x1223", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(4, "singleResponse", response);
                    Asn1TestUtil.assertNodeMatches(0, "certID", response.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(0, "certStatus: NULL (good)", response.getChildAt(1));
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "thisUpdate: 20240401100000Z (2024-04-01T10:00:00Z)",
                            response.getChildAt(2)
                    );
                    Asn1TestUtil.assertNodeMatches(0, "singleExtensions", response.getChildAt(3));
                }
            }
        }
    }

    @Test
    void correct_WithoutSingleExtensions() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 2,
                                        new DEROctetString(new byte[] {0x12, 0x23})
                                ),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(),
                                                // unknown
                                                new DERTaggedObject(false, 2, DERNull.INSTANCE),
                                                new DERGeneralizedTime("20240401100000Z"),
                                                new DERTaggedObject(true, 0,
                                                        new DERGeneralizedTime("20240601100000Z")
                                                ),
                                        })
                                ),
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID: 0x1223", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(4, "singleResponse", response);
                    Asn1TestUtil.assertNodeMatches(0, "certID", response.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(0, "certStatus: NULL (unknown)", response.getChildAt(1));
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "thisUpdate: 20240401100000Z (2024-04-01T10:00:00Z)",
                            response.getChildAt(2)
                    );
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "nextUpdate: 20240601100000Z (2024-06-01T10:00:00Z)",
                            response.getChildAt(3)
                    );
                }
            }
        }
    }

    @Test
    void correct_WithoutRevocationReason() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 2,
                                        new DEROctetString(new byte[] {0x12, 0x23})
                                ),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(),
                                                // revoked
                                                new DERTaggedObject(false, 1,
                                                        new DERSequence(
                                                                new DERGeneralizedTime("20220101100000Z")
                                                        )
                                                ),
                                        })
                                ),
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID: 0x1223", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(2, "singleResponse", response);
                    Asn1TestUtil.assertNodeMatches(0, "certID", response.getChildAt(0));
                    {
                        final AbstractAsn1TreeNode certStatus = response.getChildAt(1);
                        Asn1TestUtil.assertNodeMatches(1, "certStatus", certStatus);
                        Asn1TestUtil.assertNodeMatches(
                                0,
                                "revocationTime: 20220101100000Z (2022-01-01T10:00:00Z)",
                                certStatus.getChildAt(0)
                        );
                    }
                }
            }
        }
    }

    @Test
    void correct_InvalidRoot() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new ASN1Integer(42)
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Assertions.assertNull(node.getRfcFieldName());
    }

    @Test
    void correct_InvalidUnknownVersion() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 0, new ASN1Integer(9))
                        )
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode responseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "tbsResponseData", responseData);
            Asn1TestUtil.assertNodeMatches(0, "version: 9", responseData.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidRootSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSet(),
                        new DERTaggedObject(true, 2, new DERSequence()),
                        new DEROctetString(new byte[] {0}),
                        new DERSequence(),
                })
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "basicOCSPResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "SET", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT SEQUENCE", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "OCTET STRING: 0x00", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "SEQUENCE", node.getChildAt(3));
    }

    @Test
    void correct_InvalidResponseDataSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1Integer(0),
                                new ASN1Integer(1),
                                new ASN1Integer(2),
                                new ASN1Integer(3),
                                new ASN1Integer(4),
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(5, "tbsResponseData", tbsResponseData);
            for (int i = 0; i < 5; ++i) {
                Asn1TestUtil.assertNodeMatches(0, "INTEGER: " + i, tbsResponseData.getChildAt(i));
            }
        }
    }

    @Test
    void correct_InvalidNameBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 1, new DERSet())
                        )
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode responseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "tbsResponseData", responseData);
            Asn1TestUtil.assertNodeMatches(0, "[1] EXPLICIT SET", responseData.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidKeyHashBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 2, new DERBitString((byte) 0x80, 7))
                        )
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode responseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "tbsResponseData", responseData);
            Asn1TestUtil.assertNodeMatches(0, "[2] EXPLICIT BIT STRING: 0b1", responseData.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidCertsType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(),
                        new DERSequence(),
                        new DERBitString((byte) 0x80, 7),
                        new DERTaggedObject(true, 0, new DERSet()),
                })
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "basicOCSPResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "tbsResponseData", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "signatureAlgorithm", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "signature: 0b1", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SET", node.getChildAt(3));
    }

    @Test
    void correct_InvalidCertsSequenceEntryType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(),
                        new DERSequence(),
                        new DERBitString((byte) 0x80, 7),
                        new DERTaggedObject(true, 0, new DERSequence(
                                new DERTaggedObject(true, 0, new DERSequence())
                        )),
                })
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(4, "basicOCSPResponse", node);
        Asn1TestUtil.assertNodeMatches(0, "tbsResponseData", node.getChildAt(0));
        Asn1TestUtil.assertNodeMatches(0, "signatureAlgorithm", node.getChildAt(1));
        Asn1TestUtil.assertNodeMatches(0, "signature: 0b1", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "certs", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT SEQUENCE", node.getChildAt(3).getChildAt(0));
    }

    @Test
    void correct_InvalidVersionType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(
                                new DERTaggedObject(true, 0, new ASN1Enumerated(0))
                        )
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode responseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(1, "tbsResponseData", responseData);
            Asn1TestUtil.assertNodeMatches(0, "[0] EXPLICIT ENUMERATED: 0", responseData.getChildAt(0));
        }
    }

    @Test
    void correct_InvalidSingleResponseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSet()
                                )
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                Asn1TestUtil.assertNodeMatches(0, "SET", responses.getChildAt(0));
            }
        }
    }

    @Test
    void correct_EmptySingleResponseSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence()
                                )
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                Asn1TestUtil.assertNodeMatches(0, "singleResponse", responses.getChildAt(0));
            }
        }
    }

    @Test
    void correct_InvalidSingleResponseSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(new ASN1Encodable[] {
                                        new DERSequence(new ASN1Encodable[] {
                                                new ASN1Integer(0),
                                                new ASN1Integer(1),
                                                new ASN1Integer(2),
                                                new DERTaggedObject(true, 3, new ASN1Integer(3)),
                                                new DERTaggedObject(true, 4, new ASN1Integer(4)),
                                        }),
                                        new DERSequence(new ASN1Encodable[] {
                                                new ASN1Integer(0),
                                                new ASN1Integer(1),
                                                new ASN1Integer(2),
                                                new DERTaggedObject(true, 0, new ASN1Integer(3)),
                                                new DERTaggedObject(true, 1, new ASN1Integer(4)),
                                        }),
                                })
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(2, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(5, "singleResponse", response);
                    for (int i = 0; i < 3; ++i) {
                        Asn1TestUtil.assertNodeMatches(0, "INTEGER: " + i, response.getChildAt(i));
                    }
                    for (int i = 3; i < 5; ++i) {
                        Asn1TestUtil.assertNodeMatches(
                                0,
                                "[" + i + "] EXPLICIT INTEGER: " + i,
                                response.getChildAt(i)
                        );
                    }
                }
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(1);
                    Asn1TestUtil.assertNodeMatches(5, "singleResponse", response);
                    for (int i = 0; i < 3; ++i) {
                        Asn1TestUtil.assertNodeMatches(0, "INTEGER: " + i, response.getChildAt(i));
                    }
                    for (int i = 3; i < 5; ++i) {
                        Asn1TestUtil.assertNodeMatches(
                                0,
                                "[" + (i - 3) + "] EXPLICIT INTEGER: " + i,
                                response.getChildAt(i)
                        );
                    }
                }
            }
        }
    }

    @Test
    void correct_InvalidCertIdSequenceFieldsTypes() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(
                                                new DERSequence(new ASN1Encodable[] {
                                                        new DERTaggedObject(true, 0, new ASN1Integer(0)),
                                                        new DERTaggedObject(true, 1, new ASN1Integer(1)),
                                                        new DERTaggedObject(true, 2, new ASN1Integer(2)),
                                                        new DERTaggedObject(true, 3, new ASN1Integer(3)),
                                                })
                                        )
                                )
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(1, "singleResponse", response);
                    {
                        final AbstractAsn1TreeNode certId = response.getChildAt(0);
                        Asn1TestUtil.assertNodeMatches(4, "certID", certId);
                        for (int i = 0; i < 4; ++i) {
                            Asn1TestUtil.assertNodeMatches(
                                    0,
                                    "[" + i + "] EXPLICIT INTEGER: " + i,
                                    certId.getChildAt(i)
                            );
                        }
                    }
                }
            }
        }
    }

    @Test
    void correct_InvalidCertStatusBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(),
                                                new DERTaggedObject(false, 0, new ASN1Integer(0)),
                                        })
                                )
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(2, "singleResponse", response);
                    Asn1TestUtil.assertNodeMatches(0, "certID", response.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(0, "[0] IMPLICIT INTEGER: 0", response.getChildAt(1));
                }
            }
        }
    }

    @Test
    void correct_EmptyRevokedInfoSequence() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(),
                                                new DERTaggedObject(false, 1, new DERSequence()),
                                        })
                                )
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(2, "singleResponse", response);
                    Asn1TestUtil.assertNodeMatches(0, "certID", response.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(0, "certStatus", response.getChildAt(1));
                }
            }
        }
    }

    @Test
    void correct_InvalidRevokedInfoBaseType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(),
                                                new DERTaggedObject(false, 1, new DERSet()),
                                        })
                                )
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(2, "singleResponse", response);
                    Asn1TestUtil.assertNodeMatches(0, "certID", response.getChildAt(0));
                    Asn1TestUtil.assertNodeMatches(0, "[1] IMPLICIT SET", response.getChildAt(1));
                }
            }
        }
    }

    @Test
    void correct_InvalidRevocationReasonType() {
        final AbstractAsn1TreeNode node = Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 1, new DERSequence()),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(),
                                                new DERTaggedObject(false, 1,
                                                        new DERSequence(new ASN1Encodable[] {
                                                                new DERGeneralizedTime("20220101100000Z"),
                                                                new ASN1Enumerated(2),
                                                        })
                                                ),
                                        })
                                )
                        })
                )
        );
        BasicOcspResponseCorrector.INSTANCE.correct(node);
        Asn1TestUtil.assertNodeMatches(1, "basicOCSPResponse", node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(3, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "responderID", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(1)
            );
            {
                final AbstractAsn1TreeNode responses = tbsResponseData.getChildAt(2);
                Asn1TestUtil.assertNodeMatches(1, "responses", responses);
                {
                    final AbstractAsn1TreeNode response = responses.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(2, "singleResponse", response);
                    Asn1TestUtil.assertNodeMatches(0, "certID", response.getChildAt(0));
                    {
                        final AbstractAsn1TreeNode certStatus = response.getChildAt(1);
                        Asn1TestUtil.assertNodeMatches(2, "certStatus", certStatus);
                        Asn1TestUtil.assertNodeMatches(
                                0,
                                "revocationTime: 20220101100000Z (2022-01-01T10:00:00Z)",
                                certStatus.getChildAt(0)
                        );
                        Asn1TestUtil.assertNodeMatches(0, "ENUMERATED: 2", certStatus.getChildAt(1));
                    }
                }
            }
        }
    }

    private static void validateDefaultNode(AbstractAsn1TreeNode node, String expectedVariableName) {
        Asn1TestUtil.assertNodeMatches(4, expectedVariableName, node);
        {
            final AbstractAsn1TreeNode tbsResponseData = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(5, "tbsResponseData", tbsResponseData);
            Asn1TestUtil.assertNodeMatches(0, "version: 0 (v1)", tbsResponseData.getChildAt(0));
            Asn1TestUtil.assertNodeMatches(0, "responderID: 0x1223", tbsResponseData.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "producedAt: 20240501133700Z (2024-05-01T13:37:00Z)",
                    tbsResponseData.getChildAt(2)
            );
            Asn1TestUtil.assertNodeMatches(1, "responses", tbsResponseData.getChildAt(3));
            validateDefaultSingleResponseNode(tbsResponseData.getChildAt(3).getChildAt(0));
            {
                final AbstractAsn1TreeNode extensions = tbsResponseData.getChildAt(4);
                Asn1TestUtil.assertNodeMatches(1, "responseExtensions", extensions);
                {
                    final AbstractAsn1TreeNode extension = extensions.getChildAt(0);
                    Asn1TestUtil.assertNodeMatches(2, "extension", extension);
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "extnID: 1.3.6.1.5.5.7.48.1.5 (/iso/identified-organization/dod"
                                    + "/internet/security/mechanisms/pkix/ad/id-ad-ocsp"
                                    + "/id-pkix-ocsp-nocheck)",
                            extension.getChildAt(0)
                    );
                    Asn1TestUtil.assertNodeMatches(1, "extnValue: 0x0500", extension.getChildAt(1));
                    Asn1TestUtil.assertNodeMatches(0, "nocheck: NULL", extension.getChildAt(1).getChildAt(0));
                }
            }
        }
        {
            final AbstractAsn1TreeNode signatureAlgorithm = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "signatureAlgorithm", signatureAlgorithm);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "algorithm: 1.2.840.113549.1.1.11 (/iso/member-body/us/rsadsi/pkcs/pkcs-1"
                            + "/sha256WithRSAEncryption)",
                    signatureAlgorithm.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", signatureAlgorithm.getChildAt(1));
        }
        Asn1TestUtil.assertNodeMatches(0, "signature: 0x1122334455", node.getChildAt(2));
        Asn1TestUtil.assertNodeMatches(1, "certs", node.getChildAt(3));
        Asn1TestUtil.assertNodeMatches(0, "certificate", node.getChildAt(3).getChildAt(0));
    }

    private static void validateDefaultSingleResponseNode(AbstractAsn1TreeNode node) {
        Asn1TestUtil.assertNodeMatches(5, "singleResponse", node);
        {
            final AbstractAsn1TreeNode certId = node.getChildAt(0);
            Asn1TestUtil.assertNodeMatches(4, "certID", certId);
            {
                final AbstractAsn1TreeNode hashAlgorithm = certId.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "hashAlgorithm", hashAlgorithm);
                Asn1TestUtil.assertNodeMatches(
                        0,
                        "algorithm: 1.3.14.3.2.26 (/iso/identified-organization/oiw/secsig"
                                + "/algorithms/sha1)",
                        hashAlgorithm.getChildAt(0)
                );
                Asn1TestUtil.assertNodeMatches(0, "parameters: NULL", hashAlgorithm.getChildAt(1));
            }
            Asn1TestUtil.assertNodeMatches(0, "issuerNameHash: 0x0908", certId.getChildAt(1));
            Asn1TestUtil.assertNodeMatches(0, "issuerKeyHash: 0x0102", certId.getChildAt(2));
            Asn1TestUtil.assertNodeMatches(0, "serialNumber: 777", certId.getChildAt(3));
        }
        {
            final AbstractAsn1TreeNode certStatus = node.getChildAt(1);
            Asn1TestUtil.assertNodeMatches(2, "certStatus", certStatus);
            Asn1TestUtil.assertNodeMatches(
                    0,
                    "revocationTime: 20220101100000Z (2022-01-01T10:00:00Z)",
                    certStatus.getChildAt(0)
            );
            Asn1TestUtil.assertNodeMatches(0, "revocationReason: 2 (caCompromise)", certStatus.getChildAt(1));
        }
        Asn1TestUtil.assertNodeMatches(
                0,
                "thisUpdate: 20240401100000Z (2024-04-01T10:00:00Z)",
                node.getChildAt(2)
        );
        Asn1TestUtil.assertNodeMatches(
                0,
                "nextUpdate: 20240601100000Z (2024-06-01T10:00:00Z)",
                node.getChildAt(3)
        );
        {
            final AbstractAsn1TreeNode singleExtensions = node.getChildAt(4);
            Asn1TestUtil.assertNodeMatches(1, "singleExtensions", singleExtensions);
            {
                final AbstractAsn1TreeNode extension = singleExtensions.getChildAt(0);
                Asn1TestUtil.assertNodeMatches(2, "extension", extension);
                {
                    Asn1TestUtil.assertNodeMatches(
                            0,
                            "extnID: 1.3.6.1.5.5.7.48.1.2 (/iso/identified-organization/dod"
                                    + "/internet/security/mechanisms/pkix/ad/id-ad-ocsp"
                                    + "/id-pkix-ocsp-nonce)",
                            extension.getChildAt(0)
                    );
                    Asn1TestUtil.assertNodeMatches(1, "extnValue: 0x04021122", extension.getChildAt(1));
                    Asn1TestUtil.assertNodeMatches(0, "nonce: 0x1122", extension.getChildAt(1).getChildAt(0));
                }
            }
        }
    }

    private AbstractAsn1TreeNode createDefaultNode() {
        /*
         * No need to fill extensions/certificates here, as they can be massive...
         * It will be tested separately anyway.
         */
        return Asn1TreeNodeFactory.fromPrimitive(
                new DERSequence(new ASN1Encodable[] {
                        new DERSequence(new ASN1Encodable[] {
                                new DERTaggedObject(true, 0, new ASN1Integer(0)),
                                new DERTaggedObject(true, 2,
                                        new DEROctetString(new byte[] {0x12, 0x23})
                                ),
                                new DERGeneralizedTime("20240501133700Z"),
                                new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                new DERSequence(new ASN1Encodable[] {
                                                        new DERSequence(new ASN1Encodable[] {
                                                                new ASN1ObjectIdentifier("1.3.14.3.2.26"),  // sha1
                                                                DERNull.INSTANCE,
                                                        }),
                                                        new DEROctetString(new byte[] {0x09, 0x08}),
                                                        new DEROctetString(new byte[] {0x01, 0x02}),
                                                        new ASN1Integer(777),
                                                }),
                                                // revoked
                                                new DERTaggedObject(false, 1,
                                                        new DERSequence(new ASN1Encodable[] {
                                                                new DERGeneralizedTime("20220101100000Z"),
                                                                new DERTaggedObject(true, 0,
                                                                        new ASN1Enumerated(2)   // caCompromise
                                                                )
                                                        })
                                                ),
                                                new DERGeneralizedTime("20240401100000Z"),
                                                new DERTaggedObject(true, 0,
                                                        new DERGeneralizedTime("20240601100000Z")
                                                ),
                                                new DERTaggedObject(true, 1, new DERSequence(
                                                        new DERSequence(new ASN1Encodable[] {
                                                                // id-pkix-ocsp-nonce
                                                                new ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.2"),
                                                                new DEROctetString(new byte[] {
                                                                        0x04, 0x02, 0x11, 0x22
                                                                }),
                                                        })
                                                ))
                                        })
                                ),
                                new DERTaggedObject(true, 1, new DERSequence(
                                        new DERSequence(new ASN1Encodable[] {
                                                // id-pkix-ocsp-nocheck
                                                new ASN1ObjectIdentifier("1.3.6.1.5.5.7.48.1.5"),
                                                new DEROctetString(new byte[] {0x05, 0x00}),
                                        })
                                ))
                        }),
                        new DERSequence(new ASN1Encodable[] {
                                new ASN1ObjectIdentifier("1.2.840.113549.1.1.11"),  // sha256WithRSAEncryption
                                DERNull.INSTANCE,
                        }),
                        new DERBitString(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55}, 0),
                        new DERTaggedObject(true, 0, new DERSequence(
                                new DERSequence()
                        )),
                })
        );
    }
}