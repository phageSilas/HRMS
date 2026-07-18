package com.hrms.business.employee.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 GCM 加密/解密工具类
 * <p>
 * 用于敏感字段（身份证号、银行卡号）的加密存储
 * </p>
 */
@Slf4j
@Component
public class AesEncryptUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Value("${hrms.aes.secret-key}")
    private String secretKey;

    private static String staticSecretKey;

    @PostConstruct
    public void init() {
        // 确保密钥长度符合AES要求（16、24或32字节）
        staticSecretKey = normalizeAesKey(secretKey);
    }

    /**
     * 规范化AES密钥长度，确保其符合AES要求（16、24或32字节）
     * 
     * @param originalKey 原始密钥
     * @return 符合AES长度要求的密钥
     */
    private static String normalizeAesKey(String originalKey) {
        if (originalKey == null) {
            throw new IllegalArgumentException("AES密钥不能为空");
        }

        // AES支持的密钥长度：128位(16字节)、192位(24字节)、256位(32字节)
        // 这里我们使用256位(32字节)，这是最常用的安全级别
        int targetLength = 32;
        
        if (originalKey.length() >= targetLength) {
            // 如果原始密钥长度大于等于目标长度，截取前32个字符
            return originalKey.substring(0, targetLength);
        } else {
            // 如果原始密钥长度小于目标长度，进行填充
            StringBuilder sb = new StringBuilder(originalKey);
            while (sb.length() < targetLength) {
                // 使用原始密钥循环填充直到达到目标长度
                for (int i = 0; i < originalKey.length() && sb.length() < targetLength; i++) {
                    sb.append(originalKey.charAt(i));
                }
            }
            return sb.toString();
        }
    }

    /**
     * 加密
     *
     * @param plaintext 明文
     * @return 密文（Base64 编码，包含 IV）
     */
    public static String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKey key = new SecretKeySpec(staticSecretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 将 IV 和密文拼接
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("AES 加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密
     *
     * @param ciphertext 密文（Base64 编码，包含 IV）
     * @return 明文
     */
    public static String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);

            // 提取 IV
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKey key = new SecretKeySpec(staticSecretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES 解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }
}
