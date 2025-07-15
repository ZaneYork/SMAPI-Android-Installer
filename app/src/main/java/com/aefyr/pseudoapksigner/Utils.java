package com.aefyr.pseudoapksigner;

import com.starry.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class Utils {

    static byte[] hash(byte[] bytes, String hashingAlgorithm) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(hashingAlgorithm);
        messageDigest.update(bytes);
        return messageDigest.digest();
    }

    static String base64Encode(byte[] bytes) {
        return Base64.encodeToString(bytes, 0);
    }

    static void copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = from.read(buf)) > 0) {
            to.write(buf, 0, len);
        }
    }

    static byte[] sign(String hashingAlgorithm, PrivateKey privateKey, byte[] message) throws Exception {
        Signature sign = Signature.getInstance(hashingAlgorithm + "withRSA");
        sign.initSign(privateKey);
        sign.update(message);
        return sign.sign();
    }

    static RSAPrivateKey readPrivateKey(File file) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readFile(file));
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    static byte[] readFile(File file) throws IOException {
        byte[] fileBytes = new byte[(int) file.length()];

        try(InputStream inputStream = FileUtils.getInputStream(file)){
            inputStream.read(fileBytes);
        }
        return fileBytes;
    }
}
