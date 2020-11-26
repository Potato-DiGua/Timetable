package com.potato.timetable.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object KeyStoreUtils {
    // 密钥别名
    private const val alias = "LightTimetable"

    // 加密模式
    private const val transformation = "AES/GCM/NoPadding"

    // 密钥提供者
    private const val provider = "AndroidKeyStore"

    /**
     * 从系统中获取key
     */
    @JvmStatic
    fun getKey(): SecretKey {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyStore = KeyStore.getInstance(provider)
            keyStore.load(null)
            val key = keyStore.getKey(alias, null)
            return if (key == null) {
                // 生成密钥（会自动保存在keyStore中）
                val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, provider)
                keyGenerator.init(KeyGenParameterSpec
                        .Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build())
                // 生成key
                keyGenerator.generateKey()
            } else {
                key as SecretKey
            }
        } else {
            // 低版本使用设备唯一标识符加密
            return SecretKeySpec(Build.FINGERPRINT.toByteArray().sliceArray(0..15), "AES")
        }
    }

    /**
     * 加密
     * @param data 原文本
     * @return 密文
     */
    @JvmStatic
    fun encrypt(data: String?): String {
        if (data == null || data.isEmpty()) {
            return data.orEmpty()
        }
        val cipher = Cipher.getInstance(transformation)
        val key = getKey()
        //设置解密模式
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv //获取认证标签
        val cipherText = cipher.doFinal(data.toByteArray())

        //将认证标签和密文组合保存
        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES + iv.size + cipherText.size)
        buffer.putInt(iv.size)
        buffer.put(iv)
        buffer.put(cipherText)

        return bytesToBase64(buffer.array())
    }

    /**
     * 加密
     * @param data 密文
     * @return 原文本
     */
    @JvmStatic
    fun decrypt(data: String?): String {
        if (data == null || data.isEmpty()) {
            return data.orEmpty()
        }
        val cipher = Cipher.getInstance(transformation)
        val key = getKey()
        val cipherMsg = base64ToBytes(data)

        //从密文字节流中提取认证标签和密文
        val buffer = ByteBuffer.wrap(cipherMsg)
        val ivSize = buffer.int
        val iv = ByteArray(ivSize)
        buffer.get(iv)
        val cipherText = ByteArray(buffer.remaining())
        buffer.get(cipherText)

        // 设置解密模式和GCM
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(cipherText))
    }

    /**
     * 字节流转base64(方便以文本方式存储)
     */
    @JvmStatic
    fun bytesToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    /**
     * base64转字节流
     */
    @JvmStatic
    fun base64ToBytes(base64: String): ByteArray {
        return Base64.decode(base64, Base64.DEFAULT)
    }
}