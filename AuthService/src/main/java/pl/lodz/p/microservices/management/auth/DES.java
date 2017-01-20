package pl.lodz.p.microservices.management.auth;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * Created by pbubel on 19.01.17.
 */
public class DES {

    private Cipher encodecipher;
    private Cipher dedodecipher;
    private String charCoding = "US-ASCII";

    DES(SecretKey key) throws Exception {
        encodecipher = Cipher.getInstance("DES");
        dedodecipher = Cipher.getInstance("DES");
        encodecipher.init(Cipher.ENCRYPT_MODE, key);
        dedodecipher.init(Cipher.DECRYPT_MODE, key);
    }

    String encrypt(String str) throws Exception {
        byte[] ascii = str.getBytes(charCoding);
        byte[] enc = encodecipher.doFinal(ascii);
        return new sun.misc.BASE64Encoder().encode(enc).replace("\n", "");
    }

    String decrypt(String str) throws Exception {
        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
        byte[] ascii = dedodecipher.doFinal(dec);
        return new String(ascii, charCoding);
    }
}