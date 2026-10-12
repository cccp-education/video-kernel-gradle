# video-kernel

[![Test](https://github.com/cccp-education/video-kernel-gradle/actions/workflows/test.yml/badge.svg)](https://github.com/cccp-education/video-kernel-gradle/actions/workflows/test.yml)

N1 shared library — the **engine-neutral kernel** extracted from `capsule-gradle`
(EPIC `CAP-ENGINE-DECOUPLING`, US-5).

It carries the neutral video production model and ports, free of any renderer
(reveal.js, Playwright, AsciiDoc) or build-system (Gradle) dependency:

- `capsule.kernel.VideoKind` / `VideoSegment` / `VideoScript`
- `capsule.kernel.VisualSource` / `FileVisualSource`
- `capsule.kernel.VideoProductionEntry` / `VideoProductionPlan`
- `capsule.kernel.VideoEngine` (port that renders a visual source into a video)
- `capsule.kernel.SegmentSource` (port that turns a source into segments)

Coordinates: `education.cccp:video-kernel:0.0.1` (Apache 2.0).

The kernel boundary is guarded by `capsule.ci.KernelBoundaryTest`: the kernel
imports only `java.`/`kotlin.`/`contracts.`/`capsule.kernel.` and its code carries
no `slide`/`deck`/`reveal`/`playwright` vocabulary.

Consumers: `capsule-gradle` (N2, reveal.js engine adapter today), and later
`hyperframes-gradle` (Watts) as a second engine.
