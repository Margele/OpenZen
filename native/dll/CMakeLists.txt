set(JAR_SOURCE "${CMAKE_SOURCE_DIR}/zen.jar")
set(JAR_STAGED "${CMAKE_CURRENT_BINARY_DIR}/zen.jar")

if(NOT EXISTS ${JAR_SOURCE})
    message(WARNING
        "zen.jar not found at ${JAR_SOURCE}; "
        "did you run the Gradle 'stageNativeJar' task first? "
        "The DLL will be built but the embedded jar resource will be empty.")
    file(WRITE ${JAR_STAGED} "")
else()
    configure_file(${JAR_SOURCE} ${JAR_STAGED} COPYONLY)
endif()

add_library(OpenZen SHARED
    src/main.cpp
    src/jvm_attach.cpp
    src/jar_extract.cpp
    src/class_loader.cpp
    src/diagnostics.cpp
    res/openzen.rc
)

target_include_directories(OpenZen PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/src
    ${CMAKE_CURRENT_SOURCE_DIR}/res
)

# rc.exe needs to find zen.jar (staged into CMAKE_CURRENT_BINARY_DIR by the
# configure_file above) when assembling RCDATA from openzen.rc.
set_source_files_properties(res/openzen.rc PROPERTIES
    COMPILE_FLAGS "/I\"${CMAKE_CURRENT_BINARY_DIR}\""
)

target_compile_definitions(OpenZen PRIVATE
    OPENZEN_DLL_EXPORTS
)

set_target_properties(OpenZen PROPERTIES
    OUTPUT_NAME "OpenZen"
    PREFIX ""
)

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
# Link MinHook library and iphlpapi
target_link_libraries(OpenZen PRIVATE minhook Iphlpapi)
=======
# Link MinHook library
target_link_libraries(OpenZen PRIVATE minhook)
>>>>>>> parent of 48d68a0 (Link iphlpapi and fix includes/min usage)
=======
# Link MinHook library
target_link_libraries(OpenZen PRIVATE minhook)
>>>>>>> parent of 48d68a0 (Link iphlpapi and fix includes/min usage)
=======
# Link MinHook library
target_link_libraries(OpenZen PRIVATE minhook)
>>>>>>> parent of 48d68a0 (Link iphlpapi and fix includes/min usage)

=======
>>>>>>> parent of e2b98f5 (Add HWID spoofing and MAC hooks)
# We do not link jvm.dll: jvm.dll is already loaded in the host process when the
# DLL is injected, and JNI_GetCreatedJavaVMs is provided by it. We only need
# the JNI headers at compile time.
