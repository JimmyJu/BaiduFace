/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/watchdog.h>
#include <jni.h>
#include <android/log.h>

#define DEVICE_WG_INPUT "/dev/wiegand_input"
#define DEVICE_WG_OUTPUT "/dev/wiegand_output"


#define WG_CMD	0xFB
#define WG_26_CMD		_IO(WG_CMD, 0x01)
#define WG_34_CMD		_IO(WG_CMD, 0x02)

#define DELAY   	usleep(100*1000)

#define  LOG_TAG  "wiegand"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static int fd_input  = -1 ;
static char buffer[20];
int result = 0;
static int fd_output = -1;
unsigned char wg_output_buff[4];
unsigned short wg_hid;
unsigned short wg_pid;


#ifndef _Included_com_baidu_idl_face_main_api_Wiegand
#define _Included_com_baidu_idl_face_main_api_Wiegand
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_baidu_idl_face_main_api_Wiegand
 * Method:    inputOpen
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_baidu_idl_face_main_api_Wiegand_inputOpen
  (JNIEnv * env, jclass cls)
{

    int ret;

//	LOGD("====Wiegand_inputOpen");
    if(fd_input > 0)return fd_input;

    ret = open(DEVICE_WG_INPUT, O_RDWR);
    if(ret > 0)
	{
	    fd_input = ret;
	}
    else{
//    	LOGD("open Wiegand_inputOpen failed\n");
       	return ret;
    }
	return fd_input;
}

/*
 * Class:     com_baidu_idl_face_main_api_Wiegand
 * Method:    inputClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_baidu_idl_face_main_api_Wiegand_inputClose
  (JNIEnv * env, jclass cls)
  {
       if ( fd_input > 0 ){
           close(fd_input);
           fd_input = -1 ;
       }
       return ;
  }

/*
 * Class:     com_baidu_idl_face_main_api_Wiegand
 * Method:    inputRead
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_baidu_idl_face_main_api_Wiegand_inputRead
  (JNIEnv * env, jclass cls)
  {
   	int ret = -1;
  	if (fd_input <= 0)
  		return ret;
  	read(fd_input, &buffer, sizeof(buffer));
  	result = atoi(buffer);
//  	LOGD("read wiegand input data=0x%x result=%d\n",result,result);

  	return result;
  }
/*
 * Class:     com_baidu_idl_face_main_api_Wiegand
 * Method:    outputOpen
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_baidu_idl_face_main_api_Wiegand_outputOpen
  (JNIEnv * env, jclass cls)
  {
      int ret;
      if(fd_output > 0)return fd_output;
      ret = open(DEVICE_WG_OUTPUT,O_RDONLY | O_NONBLOCK);
      if(ret > 0)
  	   {
  	       fd_output = ret;
  	   }
      else{
         	return ret;
      }
  	return fd_output;
  }

/*
 * Class:     com_baidu_idl_face_main_api_Wiegand
 * Method:    outputClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_baidu_idl_face_main_api_Wiegand_outputClose
  (JNIEnv * env, jclass cls)
  {
       if ( fd_output > 0 ){
           close(fd_output);
           fd_output = -1 ;
       }
       return ;
  }

/*
 * Class:     com_baidu_idl_face_main_api_Wiegand
 * Method:    readoutputWrite26
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_baidu_idl_face_main_api_Wiegand_readoutputWrite26
  (JNIEnv * env, jclass cls)
  {
   	int ret = -1;
  	if (fd_input <= 0)
  		return ret;
  	if (fd_output <= 0)
  		return ret;

  	read(fd_input, &buffer, sizeof(buffer));
  	result = atoi(buffer);
  	wg_hid = (result >> 16) & 0xFFFF;
  	wg_pid = result & 0xFFFF;
  	wg_output_buff[0] = wg_hid;
  	wg_output_buff[1] = wg_pid;
  	wg_output_buff[2] = wg_pid>>8;
  	ret = ioctl(fd_output,WG_26_CMD,wg_output_buff);
  	return ret;
  }

/*
 * Class:     com_baidu_idl_face_main_api_Wiegand
 * Method:    readoutputWrite34
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_baidu_idl_face_main_api_Wiegand_readoutputWrite34
  (JNIEnv * env, jclass cls)
  {
   	int ret = -1;
  	if (fd_input <= 0)
  		return ret;
  	if (fd_output <= 0)
  		return ret;

  	read(fd_input, &buffer, sizeof(buffer));
  	result = atoi(buffer);
  	wg_hid = (result >> 16) & 0xFFFF;
  	wg_pid = result & 0xFFFF;
  	wg_output_buff[0] = wg_hid;
  	wg_output_buff[1] = wg_pid;
  	wg_output_buff[2] = wg_pid>>8;
  	ret = ioctl(fd_output,WG_34_CMD,wg_output_buff);
  	return ret;
  }
JNIEXPORT jint JNICALL Java_com_baidu_idl_face_main_api_Wiegand_Output26
  ( JNIEnv* env,jclass cls, long value)
  {
  	int ret = -1;
  	if(fd_output <= 0)
  		return fd_output;
  	memset(wg_output_buff,0,sizeof(wg_output_buff));
  	wg_hid = (value >> 16) & 0xFFFF;
  	wg_pid = value & 0xFFFF;
  	wg_output_buff[0] = wg_hid;
  	wg_output_buff[1] = wg_pid;
  	wg_output_buff[2] = wg_pid>>8;
  	ret = ioctl(fd_output,WG_26_CMD,wg_output_buff);
  	return ret;
  }

  JNIEXPORT jint JNICALL Java_com_baidu_idl_face_main_api_Wiegand_Output34
   ( JNIEnv* env,jclass cls, long value)
   {
   	int ret = -1;
   	if(fd_output <= 0)
   		return fd_output;
   	memset(wg_output_buff,0,sizeof(wg_output_buff));
   	wg_hid = (value >> 16) & 0xFFFF;
   	wg_pid = value & 0xFFFF;
   	wg_output_buff[0] = wg_hid;
   	wg_output_buff[1] = wg_hid>>8;
   	wg_output_buff[2] = wg_pid;
   	wg_output_buff[3] = wg_pid>>8;
   	ret = ioctl(fd_output,WG_34_CMD,wg_output_buff);
   	return ret;
   }

#ifdef __cplusplus
}
#endif
#endif
