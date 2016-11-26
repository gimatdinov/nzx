package cxc.jex.tracer.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import cxc.jex.tracer.TracerLogger;

public class LogbackTracerLogger implements TracerLogger {
    static class Record {
        Marker marker;
        String message;
        Throwable throwable;
    }

    private final static Logger log = LoggerFactory.getLogger("<<Tracer>>");

    private String path;

    public LogbackTracerLogger(String path) {
        this.path = path;
    }

    private Record makeRecord(String event, String msg, Throwable t) {
        Record result = new Record();
        String[] parts = event.split("/");
        if (parts.length > 1) {
            result.marker = MarkerFactory.getDetachedMarker(parts[1]);
        }
        result.message = path + "(" + event + ") " + msg;
        result.throwable = t;
        return result;
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(String marker) {
        return log.isTraceEnabled(MarkerFactory.getDetachedMarker(marker));
    }

    @Override
    public void trace(String event, String msg) {
        Record rec = makeRecord(event, msg, null);
        if (rec.marker != null)
            log.trace(rec.marker, rec.message);
        else
            log.trace(rec.message);
    }

    @Override
    public void trace(String event, String msg, Throwable t) {
        Record rec = makeRecord(event, msg, t);
        if (rec.marker != null)
            log.trace(rec.marker, rec.message, rec.throwable);
        else
            log.trace(rec.message, rec.throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(String marker) {
        return log.isDebugEnabled(MarkerFactory.getDetachedMarker(marker));
    }

    @Override
    public void debug(String event, String msg) {
        Record rec = makeRecord(event, msg, null);
        if (rec.marker != null)
            log.debug(rec.marker, rec.message);
        else
            log.debug(rec.message);
    }

    @Override
    public void debug(String event, String msg, Throwable t) {
        Record rec = makeRecord(event, msg, t);
        if (rec.marker != null)
            log.debug(rec.marker, rec.message, rec.throwable);
        else
            log.debug(rec.message, rec.throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(String marker) {
        return log.isDebugEnabled(MarkerFactory.getDetachedMarker(marker));
    }

    @Override
    public void info(String event, String msg) {
        Record rec = makeRecord(event, msg, null);
        if (rec.marker != null)
            log.info(rec.marker, rec.message);
        else
            log.info(rec.message);
    }

    @Override
    public void info(String event, String msg, Throwable t) {
        Record rec = makeRecord(event, msg, t);
        if (rec.marker != null)
            log.info(rec.marker, rec.message, rec.throwable);
        else
            log.info(rec.message, rec.throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(String marker) {
        return log.isWarnEnabled(MarkerFactory.getDetachedMarker(marker));
    }

    @Override
    public void warn(String event, String msg) {
        Record rec = makeRecord(event, msg, null);
        if (rec.marker != null)
            log.warn(rec.marker, rec.message);
        else
            log.warn(rec.message);
    }

    @Override
    public void warn(String event, String msg, Throwable t) {
        Record rec = makeRecord(event, msg, t);
        if (rec.marker != null)
            log.warn(rec.marker, rec.message, rec.throwable);
        else
            log.warn(rec.message, rec.throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(String marker) {
        return log.isErrorEnabled(MarkerFactory.getDetachedMarker(marker));
    }

    @Override
    public void error(String event, String msg) {
        Record rec = makeRecord(event, msg, null);
        if (rec.marker != null)
            log.error(rec.marker, rec.message);
        else
            log.error(rec.message);
    }

    @Override
    public void error(String event, String msg, Throwable t) {
        Record rec = makeRecord(event, msg, t);
        if (rec.marker != null)
            log.error(rec.marker, rec.message, rec.throwable);
        else
            log.error(rec.message, rec.throwable);
    }

}
