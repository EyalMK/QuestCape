## ADDED Requirements

### Requirement: One enabled run owns its resources
Each enabled plugin run SHALL explicitly own original run authority, executors, outstanding HTTP work, registrations and timers it creates. Startup SHALL unwind partially acquired resources on failure without waiting for IO. Close SHALL be idempotent, cancel owned work promptly and leave unrelated/new-run resources untouched. Lifecycle callbacks SHALL NOT block on joins, sleeps, executor termination or IO. QuestCapePlugin SHALL primarily configure and delegate lifecycle/events; capture/readiness, refresh, sidebar publication/assets and link workflows SHALL have cohesive narrow owners rather than a replacement god object or generic component/job framework.

#### Scenario: Partial startup failure
- **WHEN** startup fails after one executor/registration is acquired and before sidebar publication
- **THEN** every acquired resource is released/cancelled without waiting and later enablement starts a separate usable run

#### Scenario: Repeated close with blocked IO
- **WHEN** shutdown is requested twice while old disk/network work remains blocked
- **THEN** shutdown returns promptly, resources are cancelled once safely and later completion cannot resurrect a view or affect the next run

### Requirement: Original authority crosses every continuation
Every queued capture, startup load, refresh, save outcome, image callback, link verification, message and EDT publication SHALL retain its originating run and applicable account/session authority. Helpers SHALL NOT recapture a later run to authorize old work. Each publication SHALL consume immutable content/fact/save/operation views for the relevant revision/session, preserving independent route/account freshness and typed pending/unsaved outcomes. Capturing observations SHALL occur on ClientThread, IO/asset preparation on workers and component mutation on EDT. Browser opening SHALL remain explicit through LinkBrowser with existing destination validation.

#### Scenario: Disable and re-enable during startup
- **WHEN** an old startup completion arrives after a new run begins
- **THEN** it cannot refresh, publish, open a browser or clear queued state for the new run

#### Scenario: Account switch during asynchronous action
- **WHEN** an A-scoped capture/save/link/message arrives after B is established
- **THEN** B receives no A facts, save status, feedback, focus or navigation effects

#### Scenario: Paired persistence outcome
- **WHEN** a new current observation is published before its save completes
- **THEN** it is paired with pending/unsaved state until its own result arrives, never an earlier record's saved status

### Requirement: Deterministic faults and measured work
Lifecycle/capture/retry behavior SHALL have controlled clock/queue/store/network seams sufficient to test stale success/failure, partial startup and bounded retry without sleeps or live game input. Existing event coalescing, structural row reuse and user-owned navigation SHALL survive extraction. New caches, concurrency machinery or optimizations SHALL require observed need and evidence rather than imitation of comparator plugins.

#### Scenario: Idle ticks and bursts
- **WHEN** unchanged ticks or a burst of supported dirty events occur
- **THEN** capture/preparation remains coalesced and rendering does not repeat source interpretation or navigate automatically
