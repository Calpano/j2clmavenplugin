package example.sharedlib;

import jsinterop.annotations.JsType;

/**
 * A shared libary, written in java an used on client side and in server code
 */
public class SharedLib {

    /**
     * @param a
     * @param b
     * @return computation and result of adding two numbers as string
     */
    public static String add(int a, int b) {
        return a + " + " + b + " = " + (a + b);
    }
}
