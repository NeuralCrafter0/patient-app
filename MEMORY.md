# Teleconsult E2EE SDK Development

## Environment Setup
- **Dependencies:**
  - `vodozemac` (Olm implementation, Apache-2.0)
  - `uniffi` (mobile bindings)
  - `tokio` (async)

## Architecture
- **Shared Core:** Rust handles all Olm Protocol state and crypto using the `vodozemac` crate.
- **Mobile Bindings:** Kotlin (Android) and Swift (iOS) call the Rust SDK via UniFFI.
- **Signaling:** Rust encrypts/decrypts the WebRTC signaling payloads (SDP/ICE).

## Roadmap
1. [x] Project initialization
2. [x] Protocol selection (Olm/Vodozemac - Apache-2.0)
3. [x] Identity and One-time key generation
4. [ ] Olm Session management (Inbound/Outbound)
5. [ ] E2EE Signaling wrapper for WebRTC
6. [ ] Mobile build scripts (Android .aar, iOS .xcframework)
