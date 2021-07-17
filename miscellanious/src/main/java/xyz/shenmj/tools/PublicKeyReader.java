package xyz.shenmj.tools;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 公钥读取器
 *
 * @author SHEN Minjiang
 */
public class PublicKeyReader {

    private static final Map<String, PublicKey> localPublicKeyCache = new HashMap<>(1);
    private static final Object localPublicKeyCacheLock = new Object();

    public static PublicKey get(String file) throws Exception {
        if (localPublicKeyCache.containsKey(file)) {
            return localPublicKeyCache.get(file);
        }

        synchronized (localPublicKeyCacheLock) {
            if (localPublicKeyCache.containsKey(file)) {
                return localPublicKeyCache.get(file);
            }

            Path path = Paths.get(file);
            if (!Files.exists(path)) {
                throw new IllegalAccessException("RSA加密公钥文件不存在");
            }
            byte[] fileBytes = Files.readAllBytes(path);
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);

            String publicKeyContent = fileContent
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pk = kf.generatePublic(spec);

            localPublicKeyCache.put(file, pk);
            return pk;
        }
    }
}
