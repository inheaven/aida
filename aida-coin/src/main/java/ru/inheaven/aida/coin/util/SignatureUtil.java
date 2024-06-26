package ru.inheaven.aida.coin.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Signature
 *
 * Signature is a HMAC-SHA256 encoded message containing: nonce, client ID and API key.
 * The HMAC-SHA256 code must be generated using a secret key that was generated with your API key.
 * This code must be converted to it's hexadecimal representation (64 uppercase characters).
 *
 * Example (Python):
 *
 * message = nonce + username + api_key
 * signature = hmac.new(API_SECRET, msg=message, digestmod=hashlib.sha256).hexdigest().upper()
 *
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.01.14 22:54
 */
public class SignatureUtil {
    public static String getSignature(long nonce, String username, String api_key, String api_secret){
        try {
            String message = nonce + username + api_key;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(api_secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes())).toUpperCase();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String getHmacSHA512(String message, String api_secret){
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(api_secret.getBytes(), "HmacSHA512");
            mac.init(secret_key);

            byte[] bytes = mac.doFinal(message.getBytes("ASCII"));

            StringBuilder hash = new StringBuilder();

            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }

            return hash.toString();
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
