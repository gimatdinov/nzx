package ru.otr.nzx.config.model;

import org.json.JSONObject;

public class ActionConfig extends Config {
	public final static String ENABLE = "enable";
	public final static String ACTION_CLASS = "action_class";
	public final static String PROCESSOR_NAME = "processor_name";
	public final static String PARAMETERS = "parameters";

	public boolean enable;
	public final String action_class;
	public final String processor_name;
	public final SimpleConfig parameters;

	public ActionConfig(JSONObject src, Config host) throws ConfigException {
		super(src.getString(NAME), host);
		enable = src.optBoolean(ENABLE, true);
		action_class = src.optString(ACTION_CLASS, null);
		processor_name = src.optString(PROCESSOR_NAME, null);
		if (action_class == null && processor_name == null) {
			throw new ConfigException(ACTION_CLASS + " or " + PROCESSOR_NAME + " required!");
		}
		if (action_class != null && processor_name != null) {
			throw new ConfigException("Incompatible {" + ACTION_CLASS + ", " + PROCESSOR_NAME + "}");
		}
		if (enable && action_class != null) {
			try {
				Class.forName(action_class);
			} catch (ClassNotFoundException e) {
				throw new ConfigException(src.toString(), e);
			}
		}
		if (processor_name != null) {
			Config processorConfig = context.get(REF + processor_name);
			if (processorConfig == null) {
				throw new ConfigException("Processor with " + REF + processor_name + " not found, need for actions[name=\"" + getName() + "\"]");
			}
			if (!(processorConfig instanceof ProcessorConfig)) {
				throw new ConfigException(REF + processor_name + "\"] is not reference to Processor, need for actions[name=\"" + getName() + "\"]");
			}
			if (!((ProcessorConfig) processorConfig).enable) {
				throw new ConfigException("Processor with " + REF + processor_name + " not enable, need for actions[name=\"" + getName() + "\"]");
			}
		}
		parameters = new SimpleConfig(src.optJSONObject(PARAMETERS), PARAMETERS, this);
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put(NAME, name);
		json.put(ENABLE, enable);
		json.put(ACTION_CLASS, action_class);
		json.put(PROCESSOR_NAME, processor_name);
		json.put(PARAMETERS, parameters.toJSON());
		return json;
	}

}
