package com.gpio_ctrl;

public class GPIOControl {
    // 定义GPIO输入输出方式和高低电平
    public final static int GPIO_DIRECTION_IN = 0;
    public final static int GPIO_DIRECTION_OUT = 1;
    public final static int GPIO_VALUE_LOW = 0;
    public final static int GPIO_VALUE_HIGH = 1;
    static {
        System.loadLibrary("GPIOControl");
    }
    public final static native int exportGpio(int gpio);
    public final static native int setGpioDirection(int gpio, int direction);
    public final static native int readGpioStatus(int gpio);
    public final static native int writeGpioStatus(int gpio, int value);
    public final static native int unexportGpio(int gpio);
}
