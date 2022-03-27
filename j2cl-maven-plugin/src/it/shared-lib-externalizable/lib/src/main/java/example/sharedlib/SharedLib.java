package example.sharedlib;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A shared library, written in java and used on client side and in server code
 */
public class SharedLib implements Externalizable {

    /**
     * @param a
     * @param b
     * @return computation and result of adding two numbers as string
     */
    public static String add(int a, int b) {
        return a + " + " + b + " = " + (a + b);
    }


    @Override
    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        // JVM-only code is used here
        System.out.println("writing...");
    }

    @Override
    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // JVM-only code is used here
        System.out.println("reading...");
    }
}
