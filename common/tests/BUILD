# Tests for the common library.

package(default_visibility = ["//visibility:private"])

java_test(
    name = "TextDifferencerTest",
    srcs = ["TextDifferencerTest.java"],
    test_class = "com.google.startup.common.TextDifferencerTest",
    deps = [
        "//common:char_difference_java_proto",
        "//common:text_differencer",
        "//third_party/maven/com/google/guava",
        "//third_party/maven/junit",
    ],
)
