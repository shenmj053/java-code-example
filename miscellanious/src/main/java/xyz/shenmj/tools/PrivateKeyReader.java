package xyz.shenmj.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 私钥读取器
 *
 * @author SHEN Minjiang
 */
public class PrivateKeyReader {

    private static final Map<String, PrivateKey> localPrivateKeyCache = new HashMap<>(1);
    private static final Object localPrivateKeyCacheLock = new Object();

    public static PrivateKey get(String file) {
        if (localPrivateKeyCache.containsKey(file)) {
            return localPrivateKeyCache.get(file);
        }

        synchronized (localPrivateKeyCacheLock) {
            if (localPrivateKeyCache.containsKey(file)) {
                return localPrivateKeyCache.get(file);
            }

            try {
                Path path = Paths.get(file);
                if (!Files.exists(path)) {
                    throw new IllegalStateException("RSA加密私钥文件不存在");
                }
                byte[] fileBytes = Files.readAllBytes(path);
                String fileContent = new String(fileBytes, StandardCharsets.UTF_8);

                // should be a normal PKCS8 format
                String privateKeyContent = fileContent
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "");
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);

                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey pk = kf.generatePrivate(spec);

                localPrivateKeyCache.put(file, pk);
                return pk;
            } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
                throw new IllegalStateException("加载RSA加密私钥文件异常", e);
            }
        }
    }
}
