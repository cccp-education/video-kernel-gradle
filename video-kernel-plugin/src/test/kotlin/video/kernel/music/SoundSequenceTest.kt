package video.kernel.music

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * TDD — pure sound sequence and its validation (HF-MUSIC-LIBRARY US-2 / D12).
 */
class SoundSequenceTest {
    private fun track(
        id: String,
        mood: String = "",
    ) = MusicTrack(id = id, title = id, file = "audio/$id.mp3", licence = MusicLicence.OWN, mood = mood)

    private val library =
        MusicLibrary(
            tracks =
                listOf(
                    track("calm-01", mood = "calm"),
                    track("epic-01", mood = "epic"),
                    MusicTrack(id = "risky", title = "risky", file = "audio/risky.mp3", licence = MusicLicence.UNKNOWN),
                ),
        )

    @Test
    fun `a sequence of resolvable cues is valid`() {
        val sequence =
            SoundSequence(
                listOf(
                    SoundCue(sceneId = "intro", mood = "calm"),
                    SoundCue(sceneId = "demo", trackId = "epic-01", volume = 0.2, fadeInSeconds = 1.0),
                    SoundCue(sceneId = "outro", stop = true),
                ),
            )

        assertThat(SoundSequenceValidator.validate(sequence, library)).isEqualTo(SoundSequenceValidation.Valid)
    }

    @Test
    fun `an empty sequence is valid and means no sound layer`() {
        assertThat(SoundSequenceValidator.validate(SoundSequence(), library))
            .isEqualTo(SoundSequenceValidation.Valid)
    }

    @Test
    fun `an unknown track is reported per scene`() {
        val verdict = SoundSequenceValidator.validate(SoundSequence(listOf(SoundCue("intro", trackId = "nope"))), library)

        assertThat(verdict).isInstanceOf(SoundSequenceValidation.Invalid::class.java)
        assertThat((verdict as SoundSequenceValidation.Invalid).reasons)
            .singleElement()
            .asString()
            .contains("scene 'intro'")
            .contains("unknown track id 'nope'")
    }

    @Test
    fun `an undeclared licence is reported`() {
        val verdict = SoundSequenceValidator.validate(SoundSequence(listOf(SoundCue("intro", trackId = "risky"))), library)

        assertThat((verdict as SoundSequenceValidation.Invalid).reasons)
            .singleElement()
            .asString()
            .contains("not usable")
    }

    @Test
    fun `two cues for the same scene are rejected`() {
        val verdict =
            SoundSequenceValidator.validate(
                SoundSequence(listOf(SoundCue("intro", mood = "calm"), SoundCue("intro", mood = "epic"))),
                library,
            )

        assertThat((verdict as SoundSequenceValidation.Invalid).reasons)
            .anySatisfy { assertThat(it).contains("duplicate sound cue for scene 'intro'") }
    }

    @Test
    fun `a cue must declare a track, a mood or stop`() {
        assertThatThrownBy { SoundCue(sceneId = "intro") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `volume outside zero one is rejected`() {
        assertThatThrownBy { SoundCue(sceneId = "intro", mood = "calm", volume = 1.5) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `negative fades are rejected`() {
        assertThatThrownBy { SoundCue(sceneId = "intro", mood = "calm", fadeInSeconds = -1.0) }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { SoundCue(sceneId = "intro", mood = "calm", fadeOutSeconds = -1.0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `a stop cue ignores the music selection`() {
        val verdict = SoundSequenceValidator.validate(SoundSequence(listOf(SoundCue("outro", stop = true))), MusicLibrary())

        assertThat(verdict).isEqualTo(SoundSequenceValidation.Valid)
    }
}
