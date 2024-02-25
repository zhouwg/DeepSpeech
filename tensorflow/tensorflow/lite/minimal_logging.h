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
#ifndef TENSORFLOW_LITE_MINIMAL_LOGGING_H_
#define TENSORFLOW_LITE_MINIMAL_LOGGING_H_

#include <cstdarg>

#include "cde_log.h"
namespace tflite {

enum LogSeverity {
  TFLITE_LOG_INFO = 0,
  TFLITE_LOG_WARNING = 1,
  TFLITE_LOG_ERROR = 2,
};

namespace logging_internal {

// Helper class for simple platform-specific console logging. Note that we
// explicitly avoid the convenience of ostream-style logging to minimize binary
// size impact.
class MinimalLogger {
 public:
  // Logging hook that takes variadic args.
  static void Log(LogSeverity severity, const char *file, int line,  const char* format, ...);

  // Logging hook that takes a formatted va_list.
  static void LogFormatted(LogSeverity severity, const char *file, int line, const char* format,
                           va_list args);

 private:
  static const char* GetSeverityName(LogSeverity severity);
};

}  // namespace logging_internal
}  // namespace tflite



#define TFLITE_LOG_PROD(severity, format, ...) \
           LOG_PRI_ORIG_IMPL(__FILE__, __FUNCTION__, __LINE__, severity, "KANTV", format, ##__VA_ARGS__);

#define TFLITE_LOG_PROD_ONCE(severity, format, ...)    \
           LOG_PRI_ORIG_IMPL(__FILE__, __FUNCTION__, __LINE__, severity, "KANTV", format, ##__VA_ARGS__);

#define TFLITE_LOG          TFLITE_LOG_PROD
#define TFLITE_LOG_ONCE     TFLITE_LOG_PROD_ONCE

#endif  // TENSORFLOW_LITE_MINIMAL_LOGGING_H_
