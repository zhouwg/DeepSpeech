# This is the entry point for --crosstool_top.
#
# The cc_toolchain rule used is found by:
#
# 1. Finding the appropriate toolchain in the CROSSTOOL file based on the --cpu
#    and --compiler command line flags (if they exist, otherwise using the
#    "default_target_cpu" / "default_toolchain" fields in the CROSSTOOL file)
# 2. Concatenating the "target_cpu" and "compiler" fields of the toolchain in
#    use and using that as a key in the map in the "toolchains" attribute
package(default_visibility = ["//visibility:public"])

load(":linaro_toolchain_config.bzl", "linaro_toolchain_config")

cc_toolchain_suite(
  name = "toolchain",
  toolchains = {
    "armv7a": ":cc-compiler-armv7a",
  },
)

filegroup(
    name = "empty",
    srcs = [],
)

filegroup(
  name = "gcc_linux_all_files",
  srcs = [
    "//third_party/toolchains/embedded/linaro-gcc72-armeabi/gcc:tool-wrappers",
    "@LinaroArmGcc72//:compiler_pieces",
  ],
)

filegroup(
  name = "gcc_linux_linker_files",
  srcs = [
    "//third_party/toolchains/embedded/linaro-gcc72-armeabi/gcc:ld",
    "//third_party/toolchains/embedded/linaro-gcc72-armeabi/gcc:ar",
    "@LinaroArmGcc72//:compiler_pieces",
  ],
)

filegroup(
  name = "gcc_linux_compiler_files",
  srcs = [
    "//third_party/toolchains/embedded/linaro-gcc72-armeabi/gcc:gcc",
    "//third_party/toolchains/embedded/linaro-gcc72-armeabi/gcc:as",
  ],
)

linaro_toolchain_config(name = "linaro_armeabi-v7a")

cc_toolchain(
  name = "cc-compiler-armv7a",
  all_files = ":gcc_linux_all_files",
  compiler_files = ":gcc_linux_compiler_files",
  toolchain_identifier = "gcc72_linaro_armhf",
  toolchain_config = ":linaro_armeabi-v7a",
  dwp_files = ":empty",
  dynamic_runtime_lib = ":empty",
  linker_files = ":gcc_linux_linker_files",
  objcopy_files = "//third_party/toolchains/embedded/linaro-gcc72-armeabi/gcc:objcopy",
  static_runtime_lib = ":empty",
  strip_files = "//third_party/toolchains/embedded/linaro-gcc72-armeabi/gcc:strip",
  supports_param_files = 0,
  visibility = ["//visibility:public"],
)
