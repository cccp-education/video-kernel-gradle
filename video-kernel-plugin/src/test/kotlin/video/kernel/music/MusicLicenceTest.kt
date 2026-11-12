package video.kernel.music

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD — pure music licence model (HF-MUSIC-LIBRARY US-2).
 */
class MusicLicenceTest {
    @Test
    fun `parses the allowed licences case and separator insensitively`() {
        assertThat(MusicLicence.parse("own")).isEqualTo(MusicLicence.OWN)
        assertThat(MusicLicence.parse("CC0")).isEqualTo(MusicLicence.CC0)
        assertThat(MusicLicence.parse("cc_by")).isEqualTo(MusicLicence.CC_BY)
        assertThat(MusicLicence.parse("cc-by")).isEqualTo(MusicLicence.CC_BY)
        assertThat(MusicLicence.parse("Royalty-Free")).isEqualTo(MusicLicence.ROYALTY_FREE)
        assertThat(MusicLicence.parse("licensed")).isEqualTo(MusicLicence.LICENSED)
    }

    @Test
    fun `anything undeclared is unknown and never usable`() {
        assertThat(MusicLicence.parse("")).isEqualTo(MusicLicence.UNKNOWN)
        assertThat(MusicLicence.parse("youtube")).isEqualTo(MusicLicence.UNKNOWN)
        assertThat(MusicLicence.parse("copyrighted")).isEqualTo(MusicLicence.UNKNOWN)
    }

    @Test
    fun `only cc-by requires attribution`() {
        assertThat(MusicLicence.CC_BY.requiresAttribution).isTrue()
        assertThat(MusicLicence.entries.filter { it.requiresAttribution })
            .containsExactly(MusicLicence.CC_BY)
    }
}
