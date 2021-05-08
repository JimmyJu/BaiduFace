# Copyright 2006 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE:= libwiegand
LOCAL_SRC_FILES:= libwiegand.c

include $(BUILD_SHARED_LIBRARY)
