package video.kernel.music

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD — pure music library and deterministic selection (HF-MUSIC-LIBRARY US-2).
 */
class MusicTrackSelectorTest {
    private fun track(
        id: String,
        mood: String = "",
        licence: MusicLicence = MusicLicence.OWN,
        file: String = "audio/$id.mp3",
    ) = MusicTrack(id = id, title = id, file = file, licence = licence, mood = mood)

    private val library =
        MusicLibrary(
            tracks =
                listOf(
                    track("calm-02", mood = "calm"),
                    track("calm-01", mood = "calm"),
                    track("epic-01", mood = "epic"),
                    track("unknown-01", mood = "calm", licence = MusicLicence.UNKNOWN),
                    track("nofile-01", mood = "calm", file = ""),
                ),
        )

    @Test
    fun `an explicit track id is selected when usable`() {
        val selection = MusicTrackSelector.select(library, trackId = "epic-01")

        assertThat(selection).isInstanceOf(MusicSelection.Selected::class.java)
        assertThat((selection as MusicSelection.Selected).track.id).isEqualTo("epic-01")
    }

    @Test
    fun `an unknown track id is rejected and never substituted`() {
        val selection = MusicTrackSelector.select(library, trackId = "missing")

        assertThat(selection).isInstanceOf(MusicSelection.Rejected::class.java)
        assertThat((selection as MusicSelection.Rejected).reason).contains("unknown track id 'missing'")
    }

    @Test
    fun `an unusable licence is rejected`() {
        val selection = MusicTrackSelector.select(library, trackId = "unknown-01")

        assertThat(selection).isInstanceOf(MusicSelection.Rejected::class.java)
        assertThat((selection as MusicSelection.Rejected).reason).contains("not usable")
    }

    @Test
    fun `a mood selects the alphabetically first usable track`() {
        val selection = MusicTrackSelector.select(library, mood = "calm")

        assertThat(selection).isInstanceOf(MusicSelection.Selected::class.java)
        assertThat((selection as MusicSelection.Selected).track.id).isEqualTo("calm-01")
    }

    @Test
    fun `an unknown mood is rejected`() {
        val selection = MusicTrackSelector.select(library, mood = "melancholic")

        assertThat(selection).isInstanceOf(MusicSelection.Rejected::class.java)
        assertThat((selection as MusicSelection.Rejected).reason).contains("no usable track for mood 'melancholic'")
    }

    @Test
    fun `without any filter the first usable track is selected`() {
        val selection = MusicTrackSelector.select(library)

        assertThat((selection as MusicSelection.Selected).track.id).isEqualTo("calm-01")
    }

    @Test
    fun `an empty library is rejected`() {
        val selection = MusicTrackSelector.select(MusicLibrary())

        assertThat(selection).isInstanceOf(MusicSelection.Rejected::class.java)
        assertThat((selection as MusicSelection.Rejected).reason).contains("no usable track")
    }

    @Test
    fun `an unusable track does not satisfy a mood`() {
        val onlyUnusable = MusicLibrary(tracks = listOf(track("x", mood = "calm", licence = MusicLicence.UNKNOWN)))

        assertThat(onlyUnusable.byMood("calm")).isEmpty()
    }
}
