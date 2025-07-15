/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.archive.block;

import com.reandroid.json.JSONObject;
import com.reandroid.utils.HexUtil;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateUtil {


    public static String printCertificate(CertificateBlock block){
        X509Certificate certificate = block.getCertificate();
        if(certificate == null){
            return "Failed to get certificate bytes = "
                    + block.getCertificateBytes().length;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Subject: ");
        builder.append(certificate.getSubjectX500Principal());
        builder.append("\nIssuer: ");
        builder.append(certificate.getIssuerX500Principal());
        Date notBefore = certificate.getNotBefore();
        Date notAfter = certificate.getNotAfter();

        builder.append("\nValidity FROM: ");
        builder.append(notBefore);
        builder.append(", TO: ");
        builder.append(notAfter);
        builder.append(", PERIOD: ");
        builder.append(notAfter.getTime() - notBefore.getTime());
        builder.append("\nSerial: ");
        builder.append(HexUtil.toHex(certificate.getSerialNumber().longValue(), 1));
        builder.append("\nOID: ");
        builder.append(certificate.getSigAlgOID());
        return builder.toString();
    }

    public static JSONObject toJson(CertificateBlock block){
        JSONObject jsonObject = new JSONObject();
        X509Certificate certificate = block.getCertificate();
        if(certificate == null){
            jsonObject.put("error", "Failed to get certificate bytes = "
                    + block.getCertificateBytes().length);
            return jsonObject;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Subject: ");
        jsonObject.put("subject", toJson(certificate.getSubjectX500Principal()));
        jsonObject.put("issuer", toJson(certificate.getIssuerX500Principal()));
        jsonObject.put("not_before", certificate.getNotBefore().getTime());
        jsonObject.put("not_after", certificate.getNotAfter().getTime());
        jsonObject.put("serial", certificate.getSerialNumber().longValue());
        jsonObject.put("oid", certificate.getSigAlgOID());
        return jsonObject;
    }
    public static JSONObject toJson(X500Principal principal){
        JSONObject jsonObject = new JSONObject();
        if(principal == null){
            return null;
        }
        jsonObject.put("name", principal.getName());
        return jsonObject;
    }
    public static X509Certificate generateCertificate(byte[] encodedForm){
        CertificateFactory factory = getCertFactory();
        if(factory == null){
            return null;
        }
        try{
            // TODO: cert bytes could be in DER format ?
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(encodedForm));
        }catch (CertificateException ignored){
            return null;
        }
    }
    private static CertificateFactory getCertFactory() {
        if (sCertFactory == null) {
            try {
                sCertFactory = CertificateFactory.getInstance("X.509");
            } catch (CertificateException ignored) {
            }
        }
        return sCertFactory;
    }

    private static CertificateFactory sCertFactory = null;
}
