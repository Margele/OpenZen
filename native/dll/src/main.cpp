#include "openzen.h"

#include <atomic>

namespace openzen {
    HMODULE g_self_module = nullptr;
}

namespace {

std::atomic<bool> g_already_attached{false};

DWORD WINAPI inject_thread(LPVOID) {
    using namespace openzen;

    log::init();
    log::info("OpenZen.dll bootstrap thread started, pid=%lu", GetCurrentProcessId());

    JavaVM* vm = jvm::find_vm();
    if (!vm) return 1;

    JNIEnv* env = nullptr;
    JavaVMAttachArgs args{};
    args.version = JNI_VERSION_1_8;
    args.name = const_cast<char*>("OpenZen-Bootstrap");
    args.group = nullptr;
    if (vm->AttachCurrentThreadAsDaemon((void**)&env, &args) != JNI_OK || !env) {
        log::error("AttachCurrentThreadAsDaemon failed");
        return 2;
    }
    log::info("Attached bootstrap thread to JavaVM");

    std::wstring jar_path;
    if (!jar::extract_embedded(jar_path)) {
        vm->DetachCurrentThread();
        return 3;
    }

    jint rc = jvm::attach_instrument(vm, jar_path);
    if (rc != 0) {
        log::error("Agent_OnAttach reported error %d", (int)rc);
        // Continue anyway - some JDK builds report non-zero even on success
        // because of secondary cleanup; PatchAgent.agentmain may still have run.
    }

    jobject game_loader = classes::find_game_class_loader(vm, env);
    if (!game_loader) {
        vm->DetachCurrentThread();
        return 4;
    }

    jclass bridge_cls = classes::load_dll_bootstrap(env, game_loader, jar_path);
    if (!bridge_cls) {
        env->DeleteLocalRef(game_loader);
        vm->DetachCurrentThread();
        return 5;
    }

    jmethodID load_mid = env->GetStaticMethodID(bridge_cls, "load",
            "(Ljava/lang/String;Ljava/lang/ClassLoader;)V");
    if (!load_mid) {
        log::error("GameLoaderBridge.load(String, ClassLoader) method not found");
        env->ExceptionClear();
        vm->DetachCurrentThread();
        return 6;
    }

    jstring jar_jstr = env->NewString(
        reinterpret_cast<const jchar*>(jar_path.c_str()),
        static_cast<jsize>(jar_path.size()));

    env->CallStaticVoidMethod(bridge_cls, load_mid, jar_jstr, game_loader);
    if (env->ExceptionCheck()) {
        log::error("GameLoaderBridge.load threw an exception");
        env->ExceptionDescribe();
        env->ExceptionClear();
    } else {
        log::info("GameLoaderBridge.load returned without exception");
    }

    env->DeleteLocalRef(jar_jstr);
    env->DeleteLocalRef(bridge_cls);
    env->DeleteLocalRef(game_loader);
    vm->DetachCurrentThread();
    return 0;
}

} // namespace

BOOL APIENTRY DllMain(HMODULE module, DWORD reason, LPVOID) {
    if (reason == DLL_PROCESS_ATTACH) {
        // Idempotence: if the loader injects twice (or the host calls
        // LoadLibrary twice from different threads) we still only kick off the
        // bootstrap once.
        bool expected = false;
        if (!g_already_attached.compare_exchange_strong(expected, true)) {
            return TRUE;
        }
        openzen::g_self_module = module;
        DisableThreadLibraryCalls(module);
        // Never call JNI from inside DllMain - the loader lock is held. Kick
        // a separate worker thread that will do all the heavy lifting.
        HANDLE t = CreateThread(nullptr, 0, inject_thread, nullptr, 0, nullptr);
        if (t) CloseHandle(t);
    }
    return TRUE;
}
