package com.gpio_ctrl;

import java.io.DataOutputStream;

public class GpioCtrlService {
    // 定义GPIO输入输出方式和高低电平
    public final static int GPIO_DIRECTION_IN = 0;
    public final static int GPIO_DIRECTION_OUT = 1;
    public final static int GPIO_VALUE_LOW = 0;
    public final static int GPIO_VALUE_HIGH = 1;

    static {
        System.loadLibrary("GPIOControl");
    }

    public final static boolean openGpio(int gpio) {
        return RootCommand("echo "+gpio+" > /sys/class/gpio/export");
    }

    public final static boolean setGpioDir(int gpio,int inOutSet) {
        if(RootCommand("chmod -R 777 /sys/class/gpio/gpio"+gpio+"/direction") == false){
            return false;
        }
        GPIOControl.setGpioDirection(gpio,inOutSet);
        return true;
    }

    public final static boolean setGpioValue(int gpio,int value) {
        if(RootCommand("chmod -R 777 /sys/class/gpio/gpio"+gpio+"/value") == false){
            return false;
        }
        GPIOControl.writeGpioStatus(gpio,value);
        return true;
    }

    public final static int getGpioValue(int gpio) {
        if(RootCommand("chmod -R 777 /sys/class/gpio/gpio"+gpio+"/value") == false){
            return -1;
        }
        return GPIOControl.readGpioStatus(gpio);
    }

    public final static boolean closeGpio(int gpio) {
        return RootCommand("echo "+gpio+" > /sys/class/gpio/unexport");
    }

    public final static boolean RootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }
}
