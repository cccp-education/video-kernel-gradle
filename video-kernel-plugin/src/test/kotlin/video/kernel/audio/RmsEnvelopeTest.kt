package video.kernel.audio

import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit tests for [RmsEnvelope] (VK-4 US-4e).
 *
 * Pure — no I/O, no audio decoder. The envelope is the shared input of the
 * silence and SNR checks in both engines (capsule + hyperframes).
 */
class RmsEnvelopeTest {

    private fun pcm(vararg samples: Int): ByteArray {
        val out = ByteArrayOutputStream()
        samples.forEach { sample ->
            out.write(sample and 0xFF)
            out.write((sample shr 8) and 0xFF)
        }
        return out.toByteArray()
    }

    @Test
    fun `silence yields a zero envelope`() {
        val frames = RmsEnvelope.framesFromPcm(pcm(0, 0, 0, 0), bytesPerSample = 2, frameSamples = 2)
        assertEquals(2, frames.size)
        assertTrue(frames.all { it == 0.0 })
    }

    @Test
    fun `a full-scale frame yields the expected rms`() {
        val frames = RmsEnvelope.framesFromPcm(pcm(0, 32767), bytesPerSample = 2, frameSamples = 2)
        assertEquals(1, frames.size)
        assertEquals(0.7071, frames[0], 1e-3)
    }

    @Test
    fun `a trailing partial frame is dropped`() {
        val frames = RmsEnvelope.framesFromPcm(pcm(1000, 1000, 1000), bytesPerSample = 2, frameSamples = 2)
        assertEquals(1, frames.size)
    }

    @Test
    fun `a signal shorter than one frame yields no frame`() {
        val frames = RmsEnvelope.framesFromPcm(pcm(1000), bytesPerSample = 2, frameSamples = 2)
        assertEquals(0, frames.size)
    }

    @Test
    fun `bytesPerSample and frameSamples must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            RmsEnvelope.framesFromPcm(pcm(0), bytesPerSample = 0, frameSamples = 2)
        }
        assertFailsWith<IllegalArgumentException> {
            RmsEnvelope.framesFromPcm(pcm(0), bytesPerSample = 2, frameSamples = 0)
        }
    }
}
