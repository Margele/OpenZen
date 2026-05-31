#pragma once

#include <windows.h>
#include <iphlpapi.h>
#include <string>
#include <vector>

namespace openzen {
namespace spoof {

// Initialize MinHook and setup GetAdaptersAddresses hook
bool init_hooks();

// Shutdown and cleanup hooks
void shutdown_hooks();

// Set the fake MAC address from Java layer via JNI
void set_fake_mac(const std::string& mac);

// Get the current fake MAC
std::string get_fake_mac();

// Convert MAC string to bytes
std::vector<BYTE> mac_string_to_bytes(const std::string& mac);

// The hooked function prototype
typedef DWORD (WINAPI *GetAdaptersAddresses_t)(
    ULONG Family,
    DWORD Flags,
    PVOID Reserved,
    PIP_ADAPTER_ADDRESSES AdapterAddresses,
    PULONG SizePointer
);

} // namespace spoof
} // namespace openzen
