---
Task ID: 1
Agent: Main Agent
Task: Fix "Failed to fetch" metadata error + Make player open with buffering when choosing a source/episode

Work Log:
- Read and analyzed all key source files: StremioAddonApi, StremioRepository, AppDatabase, PlayerViewModel, PlayerScreen, DetailScreen, DetailViewModel, MainActivity
- Identified root cause of "Failed to fetch": no retry logic, short timeouts (15s), sequential addon fetching, DAO method mismatches
- Identified player issues: episode play buttons not wired, magnet URLs passed to ExoPlayer (can't play), no buffering/resolving state
- Fixed StremioAddonApi: added retry with exponential backoff (2 retries), increased timeouts to 30s, added logging
- Fixed AppDatabase DAO: added Flow-returning methods (getInstalledAddonsFlow, getLibraryFlow, getRecentSearchesFlow) + one-shot methods (getEnabledAddons) for background operations
- Fixed StremioRepository: added parallel stream fetching (getStreamsParallel), added getMetaFromAnyAddon for resilient metadata loading, better error logging
- Redesigned PlayerViewModel: injected StremioRepository, added loadStream() with fallback resolution, added loadEpisode() with background stream fetching, proper error messages for torrent/YouTube/external streams
- Redesigned PlayerScreen: added resolving indicator ("Finding streams..."), buffering state, better error overlay with helpful messages, animated controls visibility
- Fixed DetailScreen: wired up episode play buttons (now navigates to player), added onPlayEpisode callback, improved error state with retry button
- Fixed DetailViewModel: uses getMetaFromAnyAddon for more resilient loading, better error handling
- Updated MainActivity: supports both stream chip playback and episode-based playback with shared pending state
- Committed changes locally (git push requires GitHub auth credentials)

Stage Summary:
- All 8 files modified and committed: StremioAddonApi, AppDatabase, StremioRepository, PlayerViewModel, DetailViewModel, PlayerScreen, DetailScreen, MainActivity
- Key improvements: retry logic, parallel fetching, player opens immediately with buffering, episode play buttons work, better error messages
- Cannot push to GitHub without authentication credentials - changes are committed locally only
