package com.zndbl.rpc.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author zndbl
 */
public class ThrowableUtil {

    public static String toString(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}