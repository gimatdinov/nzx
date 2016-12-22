package ru.otr.nzx.config.model;

import java.net.URISyntaxException;

import org.json.JSONObject;

public class ProcessorConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String PROCESSOR_CLASS = "processor_class";
    public final static String PROCESSOR_PARAMETERS = "processor_parameters";

    public final boolean enable;
    public final String processor_class;
    public final SimpleConfig processor_parameters;

    ProcessorConfig(JSONObject src, Config host) throws URISyntaxException {
        super(src.getString(NAME), host);
        enable = src.optBoolean(ENABLE, true);
        processor_class = src.getString(PROCESSOR_CLASS);
        try {
            Class.forName(processor_class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        processor_parameters = new SimpleConfig(src.optJSONObject(PROCESSOR_PARAMETERS), PROCESSOR_PARAMETERS, this);
        context.put(REF + getName(), this);
    }

    @Override
    public Object toJSON() {
        JSONObject json = new JSONObject();
        json.put(NAME, name);
        json.put(ENABLE, enable);
        json.put(PROCESSOR_CLASS, processor_class);
        json.put(PROCESSOR_PARAMETERS, processor_parameters.toJSON());
        return json;
    }

}