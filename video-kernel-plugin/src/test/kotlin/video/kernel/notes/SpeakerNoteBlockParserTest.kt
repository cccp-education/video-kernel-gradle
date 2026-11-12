package video.kernel.notes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit tests for [SpeakerNoteBlockParser] (HF-SPEAKER-NOTES US-1).
 *
 * Pure — no I/O, no Gradle, no renderer. The grammar is shared by capsule's
 * reveal.js adapter and hyperframes' section-based adapter; these tests pin the
 * behaviours both engines rely on (including the legacy "first note wins"
 * parity rule).
 */
class SpeakerNoteBlockParserTest {
    @Test
    fun `extracts one block per section that carries a note`() {
        val source =
            """
            = Document title
            author
            :attr: value

            == First

            visible content

            [NOTE.speaker]
            --
            First note.
            --

            == Second

            visible content

            [NOTE.speaker]
            --
            Second note.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source)

        assertEquals(2, blocks.size)
        assertEquals(SpeakerNoteBlock(title = "First", text = "First note."), blocks[0])
        assertEquals(SpeakerNoteBlock(title = "Second", text = "Second note."), blocks[1])
    }

    @Test
    fun `skips sections without a note`() {
        val source =
            """
            = Document title

            == With note

            [NOTE.speaker]
            --
            Has a note.
            --

            == Without note

            Nothing to say.
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source)

        assertEquals(1, blocks.size)
        assertEquals("With note", blocks[0].title)
    }

    @Test
    fun `ignores a note before any section`() {
        val source =
            """
            = Document title

            [NOTE.speaker]
            --
            Orphan note.
            --

            == Section

            [NOTE.speaker]
            --
            Attached note.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source)

        assertEquals(1, blocks.size)
        assertEquals("Attached note.", blocks[0].text)
    }

    @Test
    fun `preserves multi-line note bodies`() {
        val source =
            """
            = Document title

            == Section

            [NOTE.speaker]
            --
            Line one.
            Line two.
            Line three.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source)

        assertEquals("Line one.\nLine two.\nLine three.", blocks.single().text)
    }

    @Test
    fun `accepts a marker with attributes`() {
        val source =
            """
            = Document title

            == Section

            [NOTE.speaker,role="narrator"]
            --
            Note text.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source)

        assertEquals("Note text.", blocks.single().text)
    }

    @Test
    fun `ignores attribute lines preceding the heading`() {
        val source =
            """
            = Document title

            [.section,data-transition="zoom"]
            == Titled section

            [NOTE.speaker]
            --
            Note.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source)

        assertEquals("Titled section", blocks.single().title)
    }

    @Test
    fun `returns no block when nothing is authored`() {
        val source =
            """
            = Document title

            == One

            no note

            == Two

            still no note
            """.trimIndent()

        assertTrue(SpeakerNoteBlockParser.parse(source).isEmpty())
    }

    @Test
    fun `trims whitespace around the title`() {
        val source =
            """
            = Document title

            ==   Spaced title

            [NOTE.speaker]
            --
            Note.
            --
            """.trimIndent()

        assertEquals("Spaced title", SpeakerNoteBlockParser.parse(source).single().title)
    }

    @Test
    fun `only the configured heading level starts a section`() {
        val source =
            """
            = Document title

            == Real section

            [NOTE.speaker]
            --
            Kept.
            --

            === Subsection

            [NOTE.speaker]
            --
            Dropped.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source, headingLevel = 2)

        assertEquals(1, blocks.size)
        assertEquals("Real section", blocks[0].title)
    }

    @Test
    fun `heading level is configurable`() {
        val source =
            """
            = Section title

            [NOTE.speaker]
            --
            Level one note.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source, headingLevel = 1)

        assertEquals("Section title", blocks.single().title)
    }

    @Test
    fun `the note marker is configurable`() {
        val source =
            """
            = Document title

            == Section

            [NOTE.voice]
            --
            Custom marker note.
            --

            [NOTE.speaker]
            --
            Should be ignored.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source, noteMarker = "[NOTE.voice]")

        assertEquals(1, blocks.size)
        assertEquals("Custom marker note.", blocks[0].text)
    }

    @Test
    fun `an unclosed note is emitted at end of input`() {
        val source =
            """
            = Document title

            == Section

            [NOTE.speaker]
            --
            Never closed.
            """.trimIndent()

        assertEquals("Never closed.", SpeakerNoteBlockParser.parse(source).single().text)
    }

    @Test
    fun `a blank note body is ignored`() {
        val source =
            """
            = Document title

            == Section

            [NOTE.speaker]
            --

            --
            """.trimIndent()

        assertTrue(SpeakerNoteBlockParser.parse(source).isEmpty())
    }

    @Test
    fun `the first note block under a section wins`() {
        val source =
            """
            = Document title

            == Section

            [NOTE.speaker]
            --
            First.
            --

            [NOTE.speaker]
            --
            Second.
            --
            """.trimIndent()

        val blocks = SpeakerNoteBlockParser.parse(source)

        assertEquals(1, blocks.size)
        assertEquals("First.", blocks[0].text)
    }

    @Test
    fun `rejects a non-positive heading level`() {
        assertFailsWith<IllegalArgumentException> {
            SpeakerNoteBlockParser.parse("== Section", headingLevel = 0)
        }
    }

    @Test
    fun `rejects a blank note marker`() {
        assertFailsWith<IllegalArgumentException> {
            SpeakerNoteBlockParser.parse("== Section", noteMarker = " ")
        }
    }
}
