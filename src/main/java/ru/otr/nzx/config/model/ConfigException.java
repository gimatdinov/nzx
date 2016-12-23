package ru.otr.nzx.config.model;

public class ConfigException extends Exception {
	private static final long serialVersionUID = 7307112714882459207L;

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(Throwable cause) {
		super(cause);
	}

}
