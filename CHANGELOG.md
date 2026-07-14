# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows [Semantic Versioning](https://semver.org/).

> [!NOTE]
> The current fork uses the `0.x` release series. Older `0.9.x`–`0.12.x`
> entries at the end of this file belong to the upstream project history and
> are kept for reference.

## [Unreleased]

### Added

### Changed

### Fixed

### Security

## [0.3.2] - 2026-07-13

### Added

- Added configurable synchronization-history retention through the `SYNC_HISTORY_RETENTION_LIMIT` environment variable.
- Added a default history retention limit of 100 executions.
- Added the `INTERRUPTED` synchronization status.
- Added startup recovery for executions left in `RUNNING` state after an unexpected application shutdown.
- Added startup and post-execution history maintenance.

### Changed

- The Scheduler history now returns and displays up to the configured retention limit.
- Old synchronization executions are automatically removed while preserving the most recent entries.
- Interrupted executions now include a completion timestamp and an explanatory message.

### Documentation

- Documented the new history-retention environment variable for Docker Compose and Docker CLI deployments.

## [0.3.1] - 2026-07-13

### Security

- Prevented platform credentials and configuration values from being written to application logs.
- Reduced OpenFeign logging from `full` to `basic`.
- Disabled detailed internal error messages in HTTP responses.
- Restricted exposed Actuator endpoints to `health` and `info`.
- Disabled the Actuator `loggers` endpoint.

### Fixed

- Added `DELETE` and `OPTIONS` to the API CORS configuration.
- Corrected package-name typos in configuration and activity classes.
- Updated scheduler-related notification and log messages.
- Corrected the documented scheduler offset limits to `-1` through `7` days.

## [0.3.0] - 2026-07-13

### Added

- Added configurable rolling periods to scheduled calendar synchronization.
- Added `startOffsetDays` and `endOffsetDays` to each scheduled sync.
- Added readable rolling-period labels to the Scheduler interface.
- Added validation for scheduler offsets and invalid date ranges.
- Added safe multi-day TrainerRoad to TrainingPeaks reconciliation by processing each day independently.

### Changed

- Scheduled syncs can now process relative periods from one day before the execution date through seven days after it.
- Existing schedules remain backward compatible and default to the current day with offsets `0 → 0`.
- `Run now` and automatic scheduler executions use the configured rolling period.
- TrainerRoad to TrainingPeaks responses now aggregate copied, removed, skipped, and failed workout counts across all processed days.
- Settings panels are collapsed by default for a cleaner mobile experience.
- Updated Scheduler and Settings styling and documentation.

### Fixed

- Corrected scheduler offset validation messages.
- Applied minor responsive and layout improvements.

## Earlier fork releases

Release details for versions before `0.3.0` are available in the repository's GitHub Releases history.

## Legacy upstream history

### [0.12.3]

- Updated Java to 21.0.3.

### [0.12.2]

- Fixed future-date workout synchronization for TrainingPeaks Premium users.

### [0.12.1]

- Fixed an invisible remove button for scheduled jobs.

### [0.12.0]

- Added multi-architecture Docker images.
- Added persistent scheduled jobs.

### [0.11.1]

- Fixed the Docker image.

### [0.11.0]

- Added scheduled calendar synchronization for planned workouts.
- Added ARM64 support.
- Added support for skipping already synchronized workouts when syncing to Intervals.icu.
- Fixed null workout names from TrainingPeaks.

### [0.10.0]

- Added calendar synchronization between TrainerRoad, TrainingPeaks, and
  Intervals.icu.
- Added a Bash script for automatic calendar synchronization with cron.
- Added future-date calendar synchronization for TrainingPeaks Premium users.
- Updated platform-configuration tutorials.
- Added automatic cleanup of pasted authentication cookies.
- Fixed TrainerRoad workout load values being set to zero.
- Replaced the log-level option with Debug Mode for troubleshooting.
- Added a TrainerRoad configuration tutorial.
- Removed Strava support.

### [0.9.0]

- Added support for distance-based workout steps.
