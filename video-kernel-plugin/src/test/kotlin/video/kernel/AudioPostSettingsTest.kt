package video.kernel

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Neutral audio post-production settings (VK-4 US-4b).
 *
 * The defaults are part of the frozen contract: an engine that does not
 * configure audio post must keep its historical behaviour, so every field
 * defaults to disabled/empty.
 */
class AudioPostSettingsTest {

    @Test
    fun `defaults are all disabled and empty`() {
        val settings = AudioPostSettings()

        assertThat(settings.bgmEnabled).isFalse()
        assertThat(settings.bgmFile).isEmpty()
        assertThat(settings.bgmLevelDb).isEqualTo(-18.0)
        assertThat(settings.loudnessTargetLufs).isEqualTo(-16.0)
        assertThat(settings.duckingEnabled).isFalse()
        assertThat(settings.twoPassLoudness).isFalse()
    }

    @Test
    fun `explicit values are preserved`() {
        val settings = AudioPostSettings(
            bgmEnabled = true,
            bgmFile = "music.mp3",
            bgmLevelDb = -12.5,
            loudnessTargetLufs = -14.0,
            duckingEnabled = true,
            twoPassLoudness = true,
        )

        assertThat(settings.bgmEnabled).isTrue()
        assertThat(settings.bgmFile).isEqualTo("music.mp3")
        assertThat(settings.bgmLevelDb).isEqualTo(-12.5)
        assertThat(settings.loudnessTargetLufs).isEqualTo(-14.0)
        assertThat(settings.duckingEnabled).isTrue()
        assertThat(settings.twoPassLoudness).isTrue()
    }

    @Test
    fun `equality is structural`() {
        assertThat(AudioPostSettings(bgmEnabled = true))
            .isEqualTo(AudioPostSettings(bgmEnabled = true))
            .isNotEqualTo(AudioPostSettings())
    }
}
