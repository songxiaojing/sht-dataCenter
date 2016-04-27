package com.byw.stock.house.data.center.platform.services;

import com.byw.stock.house.platform.core.metrics.AbstractMetricMBean;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.core.services.PlatformServiceInfo;
import com.byw.stock.house.platform.log.PlatformLogger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.DynamicMBean;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;



/**
 * @author baiyanwei
 * 
 *         Dec 30, 2013
 * 
 *         Message digest service, support MD5/SHA1/HMAC/SHA1
 */
@PlatformServiceInfo(description = "MessageDigestService", configurationPath = "application/services/MessageDigestService/")
public class MessageDigestService extends AbstractMetricMBean implements IPlatformService, DynamicMBean {

    final public static String SHA1_ALGORITHM = "SHA-1";
    final public static String MD5_ALGORITHM = "MD5";
    /**
     * HMAC/SHA1 Algorithm per RFC 2104, used when generating S3 signatures.
     */
    final public static String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    /**
     * HMAC/SHA1 Algorithm per RFC 2104, used when generating S3 signatures.
     */
    final public static String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    final private static PlatformLogger theLogger = PlatformLogger.getLogger(MessageDigestService.class);

    private int _messageDigestBuffLen = 16384;

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    /**
     * Calculate the SHA1 on a string.
     * 
     * @param message
     * @return
     */
    public byte[] messageDigest(String algorithm, byte[] message, byte[] password) {

        if (message == null || message.length == 0) {
            return null;
        }
        if (algorithm.startsWith("HmacSHA") == true) {
            if (password == null || password.length == 0) {
                return null;
            }
            return messageDigestWithHmac(HMAC_SHA256_ALGORITHM, message, password);
        } else {
            return computeDigest(algorithm, message);
        }
    }

    /**
     * Calculate the MD5 on a string.
     * 
     * @param message
     * @return
     */
    public byte[] messageDigestWithMD5(byte[] message) {

        if (message == null || message.length == 0) {
            return null;
        }
        return computeDigest(MD5_ALGORITHM, message);
    }

    /**
     * Calculate the SHA1 on a string.
     * 
     * @param message
     * @return
     */
    public byte[] messageDigestWithSHA1(byte[] message) {

        if (message == null || message.length == 0) {
            return null;
        }
        return computeDigest(SHA1_ALGORITHM, message);
    }

    /**
     * Calculate the HmacSHA256 on a string. cast 0.606s
     * 
     * @param password
     * @return
     */
    public byte[] messageDigestWithHmacSHA256(byte[] message, byte[] password) {

        if (message == null || message.length == 0) {
            return null;
        }
        if (password == null || password.length == 0) {
            return null;
        }
        return messageDigestWithHmac(HMAC_SHA256_ALGORITHM, message, password);
    }

    /**
     * Calculate the HmacSHA1 on a string.
     * 
     * @param message
     * @param password
     * @return
     */
    public byte[] messageDigestWithHmacSHA1(byte[] message, byte[] password) {

        if (message == null || message.length == 0) {
            return null;
        }
        if (password == null || password.length == 0) {
            return null;
        }
        return messageDigestWithHmac(HMAC_SHA1_ALGORITHM, message, password);
    }

    /**
     * Calculate the Hmac on a string.
     * 
     * @param algorithm
     * @param message
     * @param password
     * @return
     */
    private byte[] messageDigestWithHmac(String algorithm, byte[] message, byte[] password) {

        try {
            SecretKeySpec signingKey = new SecretKeySpec(password, algorithm);
            // Acquire the MAC instance and initialize with the signing key.
            Mac mac = Mac.getInstance(algorithm);
            mac.init(signingKey);
            //
            byte[] signArray = null;
            if (message.length > _messageDigestBuffLen) {
                signArray = Arrays.copyOf(message, _messageDigestBuffLen);
            } else {
                signArray = message;
            }
            // Compute the HMAC on the digest, and set it.
            return mac.doFinal(signArray);
        } catch (NoSuchAlgorithmException e) {
            theLogger.exception(e);
        } catch (InvalidKeyException e) {
            theLogger.exception(e);
        }
        return null;
    }

    /**
     * Computes the algorithm hash of the module and returns it as a Base64 string.
     * 
     * @param algorithm
     * @param contents
     * @return
     */
    private byte[] computeDigest(String algorithm, byte[] message) {

        //
        try {
            //
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            //
            int bytesRead = message.length >= _messageDigestBuffLen ? _messageDigestBuffLen : message.length;
            messageDigest.update(message, 0, bytesRead);
            return messageDigest.digest();
            //
        } catch (NoSuchAlgorithmException e) {
            theLogger.exception(e);
        }
        return null;
    }

    /**
     * encode into hex string
     * 
     * @param digest
     * @return
     */
    public String encodeToHex(byte[] digest) {

        // hex string
        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            int v = digest[i] & 0xFF;
            if (v < 16)
                hexStr += "0";
            hexStr += Integer.toString(v, 16).toUpperCase() + " ";
        }
        return hexStr;
    }
}
