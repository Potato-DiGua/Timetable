package com.potato.timetable.util

import junit.framework.TestCase

class KeyStoreUtilsTest : TestCase() {

    fun testGetKey() {
        assertNotNull(KeyStoreUtils.getKey())
    }
    private val testData = "hello,World"
    fun testEncrypt() {
    }

    fun testDecrypt() {
        assertEquals(testData,KeyStoreUtils.decrypt(KeyStoreUtils.encrypt(testData)))
    }
}