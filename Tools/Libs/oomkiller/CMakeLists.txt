cmake_minimum_required(VERSION 2.8)
project(oomkiller)

set(CMAKE_BUILD_TYPE Release)
set(CMAKE_C_STANDARD 99)
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")

find_package(JNI REQUIRED)
include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH}/darwin ${JAVA_INCLUDE_PATH}/linux)

set(SOURCE_FILES oomkiller.c)
add_library(oomkiller SHARED ${SOURCE_FILES})