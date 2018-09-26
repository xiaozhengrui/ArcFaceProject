/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_gpio_ctrl_GPIOControl */

#ifndef _Included_com_gpio_ctrl_GPIOControl
#define _Included_com_gpio_ctrl_GPIOControl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_gpio_ctrl_GPIOControl
 * Method:    exportGpio
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_gpio_1ctrl_GPIOControl_exportGpio
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_gpio_ctrl_GPIOControl
 * Method:    setGpioDirection
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_gpio_1ctrl_GPIOControl_setGpioDirection
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_gpio_ctrl_GPIOControl
 * Method:    readGpioStatus
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_gpio_1ctrl_GPIOControl_readGpioStatus
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_gpio_ctrl_GPIOControl
 * Method:    writeGpioStatus
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_gpio_1ctrl_GPIOControl_writeGpioStatus
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_gpio_ctrl_GPIOControl
 * Method:    unexportGpio
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_gpio_1ctrl_GPIOControl_unexportGpio
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
