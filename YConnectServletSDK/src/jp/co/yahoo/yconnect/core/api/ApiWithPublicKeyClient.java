package jp.co.yahoo.yconnect.core.api;

import jp.co.yahoo.yconnect.core.oidc.PublicKeysObject;
import jp.co.yahoo.yconnect.core.util.YConnectLogger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;

public class ApiWithPublicKeyClient extends ApiClient {

    private static final String TAG = ApiWithPublicKeyClient.class.getSimpleName();

    private final String sellerId;

    private final PublicKeysObject publicKeysObject;

    public ApiWithPublicKeyClient(String accessTokenString, Path keyFilePath, String sellerId) {
        super(accessTokenString);
        this.sellerId = sellerId;
        publicKeysObject = new PublicKeysObject();
        try {
            publicKeysObject.register(sellerId, Files.readString(keyFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fetchResource(String url, String method) throws ApiClientException {
        String signature = generateAPIPublicKeySignature();
        setHeader("X-sws-signature", signature);
        setHeader("X-sws-signature-version", "1");

        super.fetchResource(url, method);
    }

    private String generateAPIPublicKeySignature() {
        String resultSignature;
        final long unixTime = LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(9)); // Asia/TokyoのUnixTime
        // 認証情報(ストアアカウントとunixtimestampを:で結合する)
        final byte[] authenticationValue = String.format("%s:%d", sellerId, unixTime).getBytes();
        try {
            RSAPublicKey key = publicKeysObject.getPublicKey(sellerId);
            Cipher publicKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding ");
            publicKeyCipher.init(Cipher.ENCRYPT_MODE, key);
            resultSignature = Base64.getEncoder().encodeToString(publicKeyCipher.doFinal(authenticationValue));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException |
                 BadPaddingException e) {
            YConnectLogger.error(TAG, e.getMessage());
            YConnectLogger.error(TAG, Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
        return resultSignature;
    }
}
