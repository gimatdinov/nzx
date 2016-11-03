package ru.otr.nzx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Server {
	protected final String name;
	protected final Logger log;

	public Server(String name) {
		this.name = name;
		this.log = LoggerFactory.getLogger(name);
	}
	
	public abstract void bootstrap();
	
	public abstract void start();
	
	public abstract void stop();

}
