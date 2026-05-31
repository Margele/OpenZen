#include "spoof.h"
#include <MinHook.h>
#include <cstring>
#include <mutex>
#include <sstream>
#include <iomanip>
#include <algorithm>
#include <jni.h>

#pragma comment(lib, "iphlpapi.lib")

namespace openzen {
namespace spoof {

// Original function pointer
static GetAdaptersAddresses_t orig_GetAdaptersAddresses = nullptr;

// Fake MAC address storage
static std::string g_fake_mac = "00:11:22:33:44:55";
static std::mutex g_mac_mutex;

// Convert MAC string ("00:11:22:33:44:55") to bytes
std::vector<BYTE> mac_string_to_bytes(const std::string& mac) {
    std::vector<BYTE> bytes;
    std::istringstream iss(mac);
    std::string byte_str;
    
    while (std::getline(iss, byte_str, ':')) {
        if (byte_str.length() == 2) {
            unsigned int byte_val;
            std::stringstream ss;
            ss << std::hex << byte_str;
            ss >> byte_val;
            bytes.push_back(static_cast<BYTE>(byte_val));
        }
    }
    
    // Ensure we have exactly 6 bytes
    while (bytes.size() < 6) {
        bytes.push_back(0);
    }
    if (bytes.size() > 6) {
        bytes.resize(6);
    }
    
    return bytes;
}

// The hooked GetAdaptersAddresses function
static DWORD WINAPI hooked_GetAdaptersAddresses(
    ULONG Family,
    DWORD Flags,
    PVOID Reserved,
    PIP_ADAPTER_ADDRESSES AdapterAddresses,
    PULONG SizePointer
) {
    // Call original function first
    DWORD result = orig_GetAdaptersAddresses(Family, Flags, Reserved, AdapterAddresses, SizePointer);
    
    // If successful and we have adapter addresses
    if (result == ERROR_SUCCESS && AdapterAddresses != nullptr) {
        std::lock_guard<std::mutex> lock(g_mac_mutex);
        std::vector<BYTE> fake_mac_bytes = mac_string_to_bytes(g_fake_mac);
        
        // Iterate through all adapters and modify MAC addresses
        PIP_ADAPTER_ADDRESSES current = AdapterAddresses;
        while (current != nullptr) {
            // Check if this is an Ethernet or WiFi adapter with a physical address
            if (current->PhysicalAddressLength == 6) {
                // Copy our fake MAC to the adapter
                memcpy(current->PhysicalAddress, fake_mac_bytes.data(), 6);
            }
            
            current = current->Next;
        }
    }
    
    return result;
}

// Set the fake MAC address from Java layer via JNI
void set_fake_mac(const std::string& mac) {
    std::lock_guard<std::mutex> lock(g_mac_mutex);
    g_fake_mac = mac;
}

// Get the current fake MAC
std::string get_fake_mac() {
    std::lock_guard<std::mutex> lock(g_mac_mutex);
    return g_fake_mac;
}

// Initialize MinHook and setup GetAdaptersAddresses hook
bool init_hooks() {
    // Initialize MinHook
    if (MH_Initialize() != MH_OK) {
        OutputDebugStringA("[OpenZen Spoof] MH_Initialize failed\n");
        return false;
    }
    
    // Create hook for GetAdaptersAddresses
    if (MH_CreateHook(
        &GetAdaptersAddresses,
        &hooked_GetAdaptersAddresses,
        reinterpret_cast<LPVOID*>(&orig_GetAdaptersAddresses)
    ) != MH_OK) {
        OutputDebugStringA("[OpenZen Spoof] MH_CreateHook failed\n");
        return false;
    }
    
    // Enable the hook
    if (MH_EnableHook(&GetAdaptersAddresses) != MH_OK) {
        OutputDebugStringA("[OpenZen Spoof] MH_EnableHook failed\n");
        return false;
    }
    
    OutputDebugStringA("[OpenZen Spoof] Hooks initialized successfully\n");
    return true;
}

// Shutdown and cleanup hooks
void shutdown_hooks() {
    MH_DisableHook(&GetAdaptersAddresses);
    MH_Uninitialize();
}

} // namespace spoof
} // namespace openzen

// JNI exports for Java layer
extern "C" {

// JNI function to set the fake MAC address from Java
JNIEXPORT void JNICALL
Java_com_heypixel_heypixelmod_obsoverlay_protocol_spoofer_FakeMac_setNativeMac(
    JNIEnv* env,
    jclass clazz,
    jstring mac
) {
    if (mac == nullptr) return;
    
    const char* mac_cstr = env->GetStringUTFChars(mac, nullptr);
    if (mac_cstr != nullptr) {
        openzen::spoof::set_fake_mac(mac_cstr);
        env->ReleaseStringUTFChars(mac, mac_cstr);
        
        char debug_msg[256];
        snprintf(debug_msg, sizeof(debug_msg), 
            "[OpenZen Spoof] Native MAC set to: %s\n", 
            openzen::spoof::get_fake_mac().c_str());
        OutputDebugStringA(debug_msg);
    }
}

} // extern "C"
