package com.multivendor.ecommerce.util;

import com.multivendor.ecommerce.exception.BadRequestException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Implements CCAvenue's published AES-128-CBC request/response encryption
 * scheme (as distributed in their Java Integration Kit): the 16-byte AES key
 * is the MD5 digest of the merchant's Working Key, and the IV is a fixed,
 * publicly-documented 16-byte array (not secret — it's the same for every
 * CCAvenue merchant, the Working Key is what actually secures this).
 *
 * NOTE: this was written from CCAvenue's publicly documented algorithm
 * without the ability to run a live test against their servers in this
 * environment. Before going live, encrypt a known request with your real
 * Working Key and confirm CCAvenue's hosted page accepts it — if their kit
 * has changed, this is the class to revisit.
 */
public final class CCAvenueCrypto {

    // Fixed IV from CCAvenue's published integration kit — identical for all merchants.
    private static final byte[] IV = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

    private CCAvenueCrypto() {}

    public static String encrypt(String plainText, String workingKey) {
        try {
            SecretKeySpec key = new SecretKeySpec(deriveKey(workingKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return toHex(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt CCAvenue request", e);
        }
    }

    public static String decrypt(String hexEncoded, String workingKey) {
        try {
            SecretKeySpec key = new SecretKeySpec(deriveKey(workingKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
            byte[] decrypted = cipher.doFinal(fromHex(hexEncoded));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // A decrypt failure almost always means the Working Key configured here
            // doesn't match the one CCAvenue encrypted with — treat as a bad request
            // rather than a 500, since it's most likely a config/tampering issue.
            throw new BadRequestException("Unable to decrypt CCAvenue response — check the configured working key");
        }
    }

    private static byte[] deriveKey(String workingKey) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return md5.digest(workingKey.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }
}
