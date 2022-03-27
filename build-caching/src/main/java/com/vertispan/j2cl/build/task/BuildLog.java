package com.vertispan.j2cl.build.task;

/**
 * Our own log api, which can forward to the calling tool's log.
 */
public interface BuildLog {

    /** NOTE: 'trace' is even finer than 'debug' in slf4j https://www.slf4j.org/api/org/apache/log4j/Level.html, but maven logging has no trace lavel */
    void debug(String msg);

    void info(String msg);

    void warn(String msg);

    void warn(String msg, Throwable t);

    void warn(Throwable t);

    void error(String msg);

    void error(String msg, Throwable t);

    void error(Throwable t);

    boolean isDebugEnabled();
}
