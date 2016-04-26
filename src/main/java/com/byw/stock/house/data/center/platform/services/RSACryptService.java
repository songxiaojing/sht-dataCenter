package com.byw.stock.house.data.center.platform.services;

import com.byw.stock.house.platform.core.metrics.AbstractMetricMBean;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.core.services.PlatformServiceInfo;
import com.byw.stock.house.platform.core.utils.Assert;
import com.byw.stock.house.platform.core.utils.Constants;
import com.byw.stock.house.platform.log.PlatformLogger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.management.DynamicMBean;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


/**
 * @author baiyanwei
 * 
 *         Dec 31, 2013
 * 
 *         This program tests the RSA cipher. Usage:<br>
 *         -genkey public private<br>
 *         -encrypt plaintext encrypted public<br>
 *         -decrypt encrypted decrypted private<br>
 */
@PlatformServiceInfo(description = "RSACryptService", configurationPath = "application/services/RSACryptService/")
public class RSACryptService extends AbstractMetricMBean implements IPlatformService, DynamicMBean {

    private static final String RSA = "RSA";
    private static final String AES = "AES";

    /**
     * AES密钥长度为128位.
     */
    final private static int AES_KEY_SIZE = 16;
    /**
     * AES密钥计算本体.
     */
    final private static String AES_KEY_PASSWORD = "AKLDJFIOEWEKFI@#%$@&*@&*23243K81%^%%${:{90";
    private static final int KEYSIZE = 512;
    private static final PlatformLogger theLogger = PlatformLogger.getLogger(RSACryptService.class);

    private SecretKeySpec _secretKey = null;

    @Override
    public void start() throws Exception {

        initAESSecreKey();
    }

    @Override
    public void stop() throws Exception {

    }

    private void initAESSecreKey() throws Exception {

        byte[] keyMessage = AES_KEY_PASSWORD.getBytes("UTF-8");
        MessageDigest shaData = MessageDigest.getInstance("SHA-1");
        byte[] secreKeySN = shaData.digest(keyMessage);
        this._secretKey = new SecretKeySpec(secreKeySN, 0, AES_KEY_SIZE, AES);
    }

    /**
     * 
     * ASE加密.
     * 
     * @param password
     * @return
     * @throws Exception
     */
    public String aseEncrypt(String password) throws Exception {

        if (Assert.isEmptyString(password) == true) {
            return "";
        }

        byte[] content = password.getBytes(Constants.UTF_8);

        content = encryptORdecrypt(content, Cipher.ENCRYPT_MODE);

        if (content == null || content.length <= 0) {
            return "";
        }

        return parseByte2HexStr(content);
    }

    /**
     * ASE解密.
     * 
     * @param password
     * @return
     */
    public String aseDecrypt(String password) throws Exception {

        if (Assert.isEmptyString(password) == true) {
            return "";
        }
        byte[] content = parseHexStr2Byte(password);
        content = encryptORdecrypt(content, Cipher.DECRYPT_MODE);

        if (content == null || content.length <= 0) {
            return "";
        }
        return new String(content);
    }

    private byte[] encryptORdecrypt(byte[] content, int type) throws Exception {

        try {
            SecretKeySpec key = new SecretKeySpec(this._secretKey.getEncoded(), AES);
            Cipher cipher = Cipher.getInstance(AES);// 创建密码器

            cipher.init(type, key);// 初始化
            byte[] result = cipher.doFinal(content);

            return result;
        } catch (Exception e) {
            theLogger.exception(e);
            throw e;

        }
    }

    /**
     * 将二进制转换成16进制.
     * 
     * @param buf
     * @return
     */
    public String parseByte2HexStr(byte buf[]) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制.
     * 
     * @param hexStr
     * @return
     */
    public byte[] parseHexStr2Byte(String hexStr) {

        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * @return
     * 
     *         create a RSAGenKey
     */
    public KeyPair createRSAGenKey() {

        try {
            KeyPairGenerator pairgen = KeyPairGenerator.getInstance(RSA);
            SecureRandom random = new SecureRandom();
            pairgen.initialize(KEYSIZE, random);
            KeyPair keyPair = pairgen.generateKeyPair();
            keyPair.getPublic().getEncoded();
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            theLogger.exception(e);
        }
        return null;
    }

    /**
     * @param publicKeyByte
     * @return
     * 
     *         decode the RSA public key from byte array.
     */
    public PublicKey decodeRSAPublicKey(byte[] publicKeyByte) {

        if (publicKeyByte == null || publicKeyByte.length == 0) {
            return null;
        }
        PublicKey publicKey = null;
        try {
            publicKey = KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(publicKeyByte));
        } catch (InvalidKeySpecException e) {
            theLogger.exception(e);
        } catch (NoSuchAlgorithmException e) {
            theLogger.exception(e);
        }
        return publicKey;
    }

    /**
     * @param privateKeyByte
     * @return
     * 
     *         decode the RSA private key from byte array.
     */
    public PrivateKey decodeRSAPrivateKey(byte[] privateKeyByte) {

        if (privateKeyByte == null || privateKeyByte.length == 0) {
            return null;
        }
        PrivateKey privateKey = null;
        try {
            privateKey = KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(privateKeyByte));
        } catch (InvalidKeySpecException e) {
            theLogger.exception(e);
        } catch (NoSuchAlgorithmException e) {
            theLogger.exception(e);
        }
        return privateKey;
    }

    /**
     * @param rsaPublicKey
     * @param contentArray
     * @return
     * 
     *         encrypt the content array
     */
    public byte[][] encrypt(PublicKey rsaPublicKey, byte[] contentArray) {

        //
        byte[][] encryptArray = new byte[2][];
        ByteArrayInputStream byteIn = null;
        ByteArrayOutputStream byteOut = null;
        //
        try {
            // create a AES key for encrypt the content.
            KeyGenerator keygen = KeyGenerator.getInstance(AES);
            SecureRandom random = new SecureRandom();
            keygen.init(random);
            SecretKey key = keygen.generateKey();

            // wrap with RSA public key
            // encrypt the AES key in RSA
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.WRAP_MODE, rsaPublicKey);
            byte[] wrappedKey = cipher.wrap(key);
            encryptArray[0] = wrappedKey;
            //
            //
            byteIn = new ByteArrayInputStream(contentArray);
            //
            byteOut = new ByteArrayOutputStream();
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            crypt(byteIn, byteOut, cipher);
            //
            encryptArray[1] = byteOut.toByteArray();
            //

        } catch (IOException e) {
            theLogger.exception(e);
        } catch (GeneralSecurityException e) {
            theLogger.exception(e);
        } finally {
            if (byteIn != null) {
                try {
                    byteIn.close();
                } catch (IOException e) {
                }
            }
            if (byteOut != null) {
                try {
                    byteOut.close();
                } catch (IOException e) {
                }
            }
        }
        return encryptArray;

    }

    /**
     * @param rsaPrivateKey
     * @param aesKeyArray
     * @param contentArray
     * @return
     * 
     *         Decrypt the byte array
     */
    public byte[] decrypt(PrivateKey rsaPrivateKey, byte[] aesKeyArray, byte[] contentArray) {

        byte[] decryptArray = null;
        ByteArrayInputStream byteIn = null;
        ByteArrayOutputStream byteOut = null;
        try {
            //
            byteIn = new ByteArrayInputStream(contentArray);
            byteOut = new ByteArrayOutputStream();
            //
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.UNWRAP_MODE, rsaPrivateKey);
            Key aeskey = cipher.unwrap(aesKeyArray, AES, Cipher.SECRET_KEY);

            cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, aeskey);

            crypt(byteIn, byteOut, cipher);
            //
            decryptArray = byteOut.toByteArray();

        } catch (IOException e) {
            theLogger.exception(e);
        } catch (GeneralSecurityException e) {
            theLogger.exception(e);
        } finally {
            if (byteIn != null) {
                try {
                    byteIn.close();
                } catch (IOException e) {
                }
            }
            if (byteOut != null) {
                try {
                    byteOut.close();
                } catch (IOException e) {
                }
            }
        }
        return decryptArray;
    }

    /**
     * Uses a cipher to transform the bytes in an input stream and sends the transformed bytes to an output stream.
     * 
     * @param in
     *            the input stream
     * @param out
     *            the output stream
     * @param cipher
     *            the cipher that transforms the bytes
     */
    private void crypt(InputStream in, OutputStream out, Cipher cipher) throws IOException, GeneralSecurityException {

        int blockSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(blockSize);
        byte[] inBytes = new byte[blockSize];
        byte[] outBytes = new byte[outputSize];

        int inLength = 0;
        //
        boolean more = true;
        while (more) {
            inLength = in.read(inBytes);
            if (inLength == blockSize) {
                int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
                out.write(outBytes, 0, outLength);
            } else
                more = false;
        }
        if (inLength > 0) {
            outBytes = cipher.doFinal(inBytes, 0, inLength);
        } else {
            outBytes = cipher.doFinal();
        }
        out.write(outBytes);
    }

    /**
     * @param fileName
     * @param saveObj
     * 
     *            save Object to file
     */
    public void saveToFile(String fileName, Object saveObj) {

        //
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(saveObj);
        } catch (FileNotFoundException e) {
            theLogger.exception(e);
        } catch (IOException e) {
            theLogger.exception(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * @param fileName
     * @return
     * 
     *         read Object from file.
     */
    public Object readFromFile(String fileName) {

        ObjectInputStream oInput = null;
        Object readObj = null;
        try {
            oInput = new ObjectInputStream(new FileInputStream(fileName));
            readObj = oInput.readObject();
        } catch (FileNotFoundException e) {
            theLogger.exception(e);
        } catch (IOException e) {
            theLogger.exception(e);
        } catch (ClassNotFoundException e) {
            theLogger.exception(e);
        } finally {
            if (oInput != null) {
                try {
                    oInput.close();
                } catch (IOException e) {
                }
            }
        }
        return readObj;
    }
}
