package video.kernel

import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * The shared post-production and narration ports are minimal **fun interfaces**
 * (VK-4 US-4b→d): a single abstract method, so any N2 implementation —
 * FFmpeg-backed, TTS-backed, or a lambda in a test — conforms without ceremony.
 */
class KernelSharedPortsTest {

    @Test
    fun `TextToSpeech is a fun interface`() {
        val engine = TextToSpeech { _, output -> output.length() > 0 }

        assertThat(engine.synthesize("hello", File("/tmp/x.mp3"))).isFalse()
    }

    @Test
    fun `AudioPostProduction is a fun interface`() {
        val processor = AudioPostProduction { _, _, settings -> settings.bgmEnabled }

        assertThat(processor.process(File("v.mp4"), File("o.mp4"), AudioPostSettings())).isFalse()
        assertThat(processor.process(File("v.mp4"), File("o.mp4"), AudioPostSettings(bgmEnabled = true))).isTrue()
    }

    @Test
    fun `SubtitleBurnIn is a fun interface`() {
        val burner = SubtitleBurnIn { _, _, _ -> true }

        assertThat(burner.burnIn(File("v.webm"), File("s.vtt"), File("o.webm"))).isTrue()
    }
}
