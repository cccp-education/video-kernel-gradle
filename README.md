# video-kernel

[![Test](https://github.com/cccp-education/video-kernel-gradle/actions/workflows/test.yml/badge.svg)](https://github.com/cccp-education/video-kernel-gradle/actions/workflows/test.yml)

N1 shared library — the **engine-neutral kernel** extracted from `capsule-gradle`
(EPIC `CAP-ENGINE-DECOUPLING`, US-5).

It carries the neutral video production model and ports, free of any renderer
(reveal.js, Playwright, AsciiDoc) or build-system (Gradle) dependency.

**Shared core** (`capsule.kernel`) — the common denominator for every engine:

- `VisualSource` / `FileVisualSource` (a file to render into a video)
- `VideoEngine` (port: `render(VisualSource) → produced video`) + `VideoRenderRequest` / `VideoRenderResult`
- `VideoOutcome` (neutral descriptor of the produced video: duration/dimensions/size/codec)
- `VideoProductionEntry` / `VideoProductionPlan` (a plan over visual sources)

**Segment-oriented capability** (`capsule.kernel.segment`) — used by segment-based
engines (reveal.js capture), not imposed on whole-document renderers (HyperFrames):

- `VideoKind` / `VideoSegment` / `VideoScript`
- `SegmentSource` (turns a source into ordered segments)

Coordinates: `education.cccp:video-kernel:0.0.3` (Apache 2.0).

The kernel boundary is guarded by `capsule.ci.KernelBoundaryTest`: the kernel
imports only `java.`/`kotlin.`/`contracts.`/`capsule.kernel.` and its code carries
no `slide`/`deck`/`reveal`/`playwright` vocabulary.

Consumers: `capsule-gradle` (N2, reveal.js engine adapter today), and later
`hyperframes-gradle` (Watts) as a second engine.
