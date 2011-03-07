package org.metadsl.mojo;

import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.Marker;


public class LoggerAdapter4Console implements Logger {

	private String name;
    private PrintStream ps;

	//TODO: "name" must be used to prefix messages


    public LoggerAdapter4Console(final PrintStream ps) {
        this.ps = ps;
    }


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	public boolean isDebugEnabled() {
		return true;
	}
	public boolean isDebugEnabled(Marker marker) {
		return true;
	}
	public boolean isErrorEnabled() {
		return true;
	}
	public boolean isErrorEnabled(Marker marker) {
		return true;
	}
	public boolean isInfoEnabled() {
		return true;
	}
	public boolean isInfoEnabled(Marker marker) {
		return true;
	}
	public boolean isTraceEnabled() {
		return true;
	}
	public boolean isTraceEnabled(Marker marker) {
		return true;
	}
	public boolean isWarnEnabled() {
		return true;
	}
	public boolean isWarnEnabled(Marker marker) {
		return true;
	}


	public void debug(String msg) {
		ps.println(msg);
	}
	public void debug(String format, Object arg) {
		ps.println(String.format(format, arg));
	}
	public void debug(String format, Object[] argArray) {
		ps.println(String.format(format, argArray));
	}
	public void debug(String msg, Throwable t) {
		ps.println(msg);
        t.printStackTrace(ps);
	}
	public void debug(Marker marker, String msg) {
		ps.println(String.format("%s : %s", marker.toString(), msg));
	}
	public void debug(String format, Object arg1, Object arg2) {
		ps.println(String.format(format, arg1, arg2));
	}
	public void debug(Marker marker, String format, Object arg) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void debug(Marker marker, String format, Object[] argArray) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void debug(Marker marker, String msg, Throwable t) {
		ps.println(String.format("%s : %s", marker.toString(), t));
	}
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void error(String msg) {
		ps.println(msg);
	}
	public void error(String format, Object arg) {
		ps.println(String.format(format, arg));
	}
	public void error(String format, Object[] argArray) {
		ps.println(String.format(format, argArray));
	}
	public void error(String msg, Throwable t) {
		ps.println(msg);
        t.printStackTrace(ps);
	}
	public void error(Marker marker, String msg) {
		ps.println(String.format("%s : %s", marker.toString(), msg));
	}
	public void error(String format, Object arg1, Object arg2) {
		ps.println(String.format(format, arg1, arg2));
	}
	public void error(Marker marker, String format, Object arg) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void error(Marker marker, String format, Object[] argArray) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void error(Marker marker, String msg, Throwable t) {
		ps.println(String.format("%s : %s", marker.toString(), t));
	}
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void info(String msg) {
		ps.println(msg);
	}
	public void info(String format, Object arg) {
		ps.println(String.format(format, arg));
	}
	public void info(String format, Object[] argArray) {
		ps.println(String.format(format, argArray));
	}
	public void info(String msg, Throwable t) {
		ps.println(msg);
        t.printStackTrace(ps);
	}
	public void info(Marker marker, String msg) {
		ps.println(String.format("%s : %s", marker.toString(), msg));
	}
	public void info(String format, Object arg1, Object arg2) {
		ps.println(String.format(format, arg1, arg2));
	}
	public void info(Marker marker, String format, Object arg) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void info(Marker marker, String format, Object[] argArray) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void info(Marker marker, String msg, Throwable t) {
		ps.println(String.format("%s : %s", marker.toString(), t));
	}
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void trace(String msg) {
		ps.println(msg);
	}
	public void trace(String format, Object arg) {
		ps.println(String.format(format, arg));
	}
	public void trace(String format, Object[] argArray) {
		ps.println(String.format(format, argArray));
	}
	public void trace(String msg, Throwable t) {
		ps.println(msg);
        t.printStackTrace(ps);
	}
	public void trace(Marker marker, String msg) {
		ps.println(String.format("%s : %s", marker.toString(), msg));
	}
	public void trace(String format, Object arg1, Object arg2) {
		ps.println(String.format(format, arg1, arg2));
	}
	public void trace(Marker marker, String format, Object arg) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void trace(Marker marker, String format, Object[] argArray) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void trace(Marker marker, String msg, Throwable t) {
		ps.println(String.format("%s : %s", marker.toString(), t));
	}
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}


	public void warn(String msg) {
		ps.println(msg);
	}
	public void warn(String format, Object arg) {
		ps.println(String.format(format, arg));
	}
	public void warn(String format, Object[] argArray) {
		ps.println(String.format(format, argArray));
	}
	public void warn(String msg, Throwable t) {
		ps.println(msg);
        t.printStackTrace(ps);
	}
	public void warn(Marker marker, String msg) {
		ps.println(String.format("%s : %s", marker.toString(), msg));
	}
	public void warn(String format, Object arg1, Object arg2) {
		ps.println(String.format(format, arg1, arg2));
	}
	public void warn(Marker marker, String format, Object arg) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg)));
	}
	public void warn(Marker marker, String format, Object[] argArray) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, argArray)));
	}
	public void warn(Marker marker, String msg, Throwable t) {
		ps.println(String.format("%s : %s", marker.toString(), t));
	}
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		ps.println(String.format("%s : %s", marker.toString(), String.format(format, arg1, arg2)));
	}

}
