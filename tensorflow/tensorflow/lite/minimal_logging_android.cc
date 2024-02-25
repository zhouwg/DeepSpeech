/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

#include "tensorflow/lite/minimal_logging.h"

#include <android/log.h>
#include <cstdio>
#include "cde_log.h"

namespace tflite {
namespace logging_internal {
namespace {

int GetPlatformSeverity(LogSeverity severity) {
  switch (severity) {
    case TFLITE_LOG_INFO:
      return ANDROID_LOG_INFO;
    case TFLITE_LOG_WARNING:
      return ANDROID_LOG_WARN;
    case TFLITE_LOG_ERROR:
      return ANDROID_LOG_ERROR;
    default:
      return ANDROID_LOG_DEBUG;
  }
}

}  // namespace

void MinimalLogger::LogFormatted(LogSeverity severity, const char *file, int line, const char* format, va_list args) {
  // First log to Android's explicit log(cat) API.
  va_list args_for_android_log;
  static unsigned char logBuf[4096];

  va_copy(args_for_android_log, args);
  memset(logBuf, 0, 4096);
  int len_prefix = snprintf((char*)logBuf, 4096, "[%s,%d]: ", file, line);
  int len_content = vsnprintf((char*)logBuf + len_prefix, 4096 - len_prefix, format, args_for_android_log);
  __android_log_print(CDE_LOG_DEBUG, "KANTV", "%s\n", logBuf);
  va_end(args_for_android_log);

  //va_copy(args_for_android_log, args);
  //LOGJ("KANTV[%s,%d]", file, line);
  //__android_log_vprint(GetPlatformSeverity(severity), "KANTV", format, args);
  //va_end(args_for_android_log);

  // Also print to stderr for standard console applications.
  fprintf(stderr, "%s: ", GetSeverityName(severity));
  vfprintf(stderr, format, args);
  fputc('\n', stderr);
}

}  // namespace logging_internal
}  // namespace tflite
