package cxc.jex.postprocessing;

import cxc.jex.tracer.Tracer;

public abstract class Action<T extends Tank> {

	private boolean enable = true;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public abstract void process(T tank, Tracer tracer);
}