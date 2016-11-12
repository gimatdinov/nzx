package cxc.jex.tracer;

public interface TracerLogger {

    boolean isTraceEnabled();

    boolean isTraceEnabled(String marker);

    void trace(String event, String msg);

    void trace(String event, String msg, Throwable t);

    boolean isDebugEnabled();

    boolean isDebugEnabled(String marker);

    void debug(String event, String msg);

    void debug(String event, String msg, Throwable t);

    boolean isInfoEnabled();

    boolean isInfoEnabled(String marker);

    void info(String event, String msg);

    void info(String event, String msg, Throwable t);

    boolean isWarnEnabled();

    boolean isWarnEnabled(String marker);

    void warn(String event, String msg);

    void warn(String event, String msg, Throwable t);

    boolean isErrorEnabled();

    boolean isErrorEnabled(String marker);

    void error(String event, String msg);

    void error(String event, String msg, Throwable t);

}