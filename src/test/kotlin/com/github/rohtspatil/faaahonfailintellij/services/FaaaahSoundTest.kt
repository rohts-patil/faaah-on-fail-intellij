package com.github.rohtspatil.faaahonfailintellij.services

import org.junit.Assert.assertEquals
import org.junit.Test

class FaaaahSoundTest {

    @Test
    fun `fromName maps stable sounds correctly`() {
        assertEquals(FaaaahSound.FAAAAH, FaaaahSound.fromName("faaaah"))
        assertEquals(FaaaahSound.FATALITY, FaaaahSound.fromName("fatality"))
        assertEquals(FaaaahSound.JOKER, FaaaahSound.fromName("joker"))
    }

    @Test
    fun `fromName falls back to default for unknown values`() {
        assertEquals(FaaaahSound.FAAAAH, FaaaahSound.fromName("unknown-sound"))
    }
}
