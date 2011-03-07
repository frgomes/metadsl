package org.metadsl.util;

import org.slf4j.Logger;
import org.slf4j.Marker;


public class LoggerAdapter4Plexus implements Logger {

	private final org.codehaus.plexus.logging.Logger logger;

	private String name;


	//TODO: "name" must be used to prefix messages


    public LoggerAdapter4Plexus(org.codehaus.plexus.logging.Logger logger) {
        this.logger = logger;
    }


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	public boolean isDebugEnabled(Marker marker) {
		return logger.isDebugEnabled();
	}
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}
	public boolean isErrorEnabled(Marker marker) {
		return logger.isErrorEnabled();
	}
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}
	public boolean isInfoEnabled(Marker marker) {
		return logger.isInfoEnabled();
	}
	public boolean isTraceEnabled() {
		return logger.isDebugEnabled();
	}
	public boolean isTraceEnabled(Marker marker) {
		return logger.isDebugEnabled();
	}
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}
	public boolean isWarnEnabled(Marker marker) {
		return logger.isWarnEnabled();
	}


	public void debug(String msg) {
		logger.debug(msg);
	}
	public void debug(String format, Object arg) {
		logger.debug(String.format(format, arg));
	}
	public void debug(String format, Object[] argArray) {
		logger.debug(String.format(format, argArray));
	}
	public void debug(String msg, Throwable t) {
		logger.debug(msg, t);
	}
	public void debug(Marker marker, String msg) {
		logger.debug(String.format("%s : %s", marker.toString(), msg));
	}
	public void debug(String format, Object arg1, Object arg2) {
		logger.debug(String.format(format, arg1, arg2));
	}
	public void debug(Marker marker, String format, Object arg) {
		logger.debug(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void debug(Marker marker, String format, Object[] argArray) {
		logger.debug(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void debug(Marker marker, String msg, Throwable t) {
		logger.debug(String.format("%s : %s", marker.toString(), t));
	}
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void error(String msg) {
		logger.error(msg);
	}
	public void error(String format, Object arg) {
		logger.error(String.format(format, arg));
	}
	public void error(String format, Object[] argArray) {
		logger.error(String.format(format, argArray));
	}
	public void error(String msg, Throwable t) {
		logger.error(msg, t);
	}
	public void error(Marker marker, String msg) {
		logger.error(String.format("%s : %s", marker.toString(), msg));
	}
	public void error(String format, Object arg1, Object arg2) {
		logger.error(String.format(format, arg1, arg2));
	}
	public void error(Marker marker, String format, Object arg) {
		logger.error(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void error(Marker marker, String format, Object[] argArray) {
		logger.error(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void error(Marker marker, String msg, Throwable t) {
		logger.error(String.format("%s : %s", marker.toString(), t));
	}
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		logger.error(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void info(String msg) {
		logger.info(msg);
	}
	public void info(String format, Object arg) {
		logger.info(String.format(format, arg));
	}
	public void info(String format, Object[] argArray) {
		logger.info(String.format(format, argArray));
	}
	public void info(String msg, Throwable t) {
		logger.info(msg, t);
	}
	public void info(Marker marker, String msg) {
		logger.info(String.format("%s : %s", marker.toString(), msg));
	}
	public void info(String format, Object arg1, Object arg2) {
		logger.info(String.format(format, arg1, arg2));
	}
	public void info(Marker marker, String format, Object arg) {
		logger.info(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void info(Marker marker, String format, Object[] argArray) {
		logger.info(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void info(Marker marker, String msg, Throwable t) {
		logger.info(String.format("%s : %s", marker.toString(), t));
	}
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		logger.info(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void trace(String msg) {
		logger.debug(msg);
	}
	public void trace(String format, Object arg) {
		logger.debug(String.format(format, arg));
	}
	public void trace(String format, Object[] argArray) {
		logger.debug(String.format(format, argArray));
	}
	public void trace(String msg, Throwable t) {
		logger.debug(msg, t);
	}
	public void trace(Marker marker, String msg) {
		logger.debug(String.format("%s : %s", marker.toString(), msg));
	}
	public void trace(String format, Object arg1, Object arg2) {
		logger.debug(String.format(format, arg1, arg2));
	}
	public void trace(Marker marker, String format, Object arg) {
		logger.debug(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void trace(Marker marker, String format, Object[] argArray) {
		logger.debug(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void trace(Marker marker, String msg, Throwable t) {
		logger.debug(String.format("%s : %s", marker.toString(), t));
	}
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void warn(String msg) {
		logger.warn(msg);
	}
	public void warn(String format, Object arg) {
		logger.warn(String.format(format, arg));
	}
	public void warn(String format, Object[] argArray) {
		logger.warn(String.format(format, argArray));
	}
	public void warn(String msg, Throwable t) {
		logger.warn(msg, t);
	}
	public void warn(Marker marker, String msg) {
		logger.warn(String.format("%s : %s", marker.toString(), msg));
	}
	public void warn(String format, Object arg1, Object arg2) {
		logger.warn(String.format(format, arg1, arg2));
	}
	public void warn(Marker marker, String format, Object arg) {
		logger.warn(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void warn(Marker marker, String format, Object[] argArray) {
		logger.warn(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void warn(Marker marker, String msg, Throwable t) {
		logger.warn(String.format("%s : %s", marker.toString(), t));
	}
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		logger.warn(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}

}
