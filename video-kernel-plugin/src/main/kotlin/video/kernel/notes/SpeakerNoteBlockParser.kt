package video.kernel.notes

/**
 * One speaker-note block extracted from a level-structured source document.
 *
 * The kernel is engine-neutral: it knows nothing about a concrete renderer or
 * document flavour. A block is the pair `(title, text)` where [title] is the
 * title of the enclosing level-based section and [text] is the note body that
 * was authored inside the marker-delimited block.
 *
 * Invariants (fail-fast):
 * - [title] must not be blank.
 * - [text] must not be blank.
 *
 * @property title title of the enclosing section, must be non-blank.
 * @property text  note body, must be non-blank.
 */
data class SpeakerNoteBlock(
    val title: String,
    val text: String,
) {
    init {
        require(title.isNotBlank()) { "SpeakerNoteBlock.title must not be blank" }
        require(text.isNotBlank()) { "SpeakerNoteBlock.text must not be blank" }
    }
}

/**
 * Neutral parser for speaker-note blocks (HF-SPEAKER-NOTES US-1).
 *
 * The grammar is shared by every engine that reads a level-structured document
 * (capsule's reveal.js adapter and hyperframes' section-based adapter): a note
 * is a marker line followed by a delimiter-delimited block, attached to the
 * **preceding** section heading.
 *
 * ```
 * == Section title
 *
 * [NOTE.speaker]
 * --
 * note body
 * --
 * ```
 *
 * The parser is configurable so each engine can adapt the document flavour
 * without duplicating the grammar:
 * - [headingLevel] selects the level that starts a section (`2` = `== `);
 *   deeper headings are **not** sections.
 * - [noteMarker] selects the marker prefix (`[NOTE.speaker]` by default);
 *   a marker carrying attributes (`[NOTE.speaker,role="x"]`) is accepted.
 *
 * Behaviour (deterministic, pure — no I/O, no Gradle, no renderer):
 * - a note before any section heading is ignored;
 * - a section without a note is skipped;
 * - a note body that is blank after trimming is ignored;
 * - the first note block under a section wins (a second block under the same
 *   heading is dropped — legacy parity with the capsule adapter);
 * - a note that is never closed is still emitted at end of input.
 */
object SpeakerNoteBlockParser {
    /** Default heading level (`2`, i.e. `== `). */
    const val DEFAULT_HEADING_LEVEL: Int = 2

    /** Default marker line (`[NOTE.speaker]`). */
    const val DEFAULT_NOTE_MARKER: String = "[NOTE.speaker]"

    /** Block delimiter line prefix (`--`). */
    const val BLOCK_DELIMITER: String = "--"

    /**
     * Extracts every speaker-note block of [source], in document order.
     *
     * @param source       the level-structured document to read.
     * @param headingLevel level that starts a section (1 = `= `, 2 = `== `).
     * @param noteMarker   marker line that opens a note block.
     * @return the note blocks in document order (empty when nothing is authored).
     */
    fun parse(
        source: String,
        headingLevel: Int = DEFAULT_HEADING_LEVEL,
        noteMarker: String = DEFAULT_NOTE_MARKER,
    ): List<SpeakerNoteBlock> {
        require(headingLevel >= 1) { "headingLevel must be >= 1 (was $headingLevel)" }
        require(noteMarker.isNotBlank()) { "noteMarker must not be blank" }

        val headingPrefix = "=".repeat(headingLevel) + " "
        val markerWithAttributes = noteMarker.removeSuffix("]") + ","

        val blocks = mutableListOf<SpeakerNoteBlock>()
        var currentTitle: String? = null
        var inNote = false
        var noteOpened = false
        val noteLines = mutableListOf<String>()

        fun flush() {
            val title = currentTitle ?: return
            val text = noteLines.joinToString("\n").trim()
            noteLines.clear()
            if (text.isBlank()) return
            blocks.add(SpeakerNoteBlock(title = title, text = text))
        }

        for (line in source.lines()) {
            val leading = line.trimStart()
            when {
                line.startsWith(headingPrefix) -> {
                    flush()
                    currentTitle = line.removePrefix(headingPrefix).trim()
                    inNote = false
                    noteOpened = false
                    noteLines.clear()
                }
                leading.startsWith(noteMarker) ||
                    (markerWithAttributes != noteMarker && leading.startsWith(markerWithAttributes)) -> {
                    inNote = true
                    noteOpened = false
                    noteLines.clear()
                }
                inNote && !noteOpened && leading.startsWith(BLOCK_DELIMITER) -> noteOpened = true
                inNote && noteOpened && leading.startsWith(BLOCK_DELIMITER) -> {
                    flush()
                    currentTitle = null
                    inNote = false
                    noteOpened = false
                    noteLines.clear()
                }
                inNote && noteOpened -> noteLines.add(line)
            }
        }
        flush()
        return blocks
    }
}
