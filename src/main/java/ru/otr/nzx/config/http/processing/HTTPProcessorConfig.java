package ru.otr.nzx.config.http.processing;

import java.net.URISyntaxException;

import org.json.JSONObject;

import ru.otr.nzx.config.Config;
import ru.otr.nzx.config.SimpleConfig;

public class HTTPProcessorConfig extends Config {
    public final static String ENABLE = "enable";
    public final static String PROCESSOR_CLASS = "processor_class";
    public final static String PROCESSOR_PARAMETERS = "processor_parameters";

    public final boolean enable;
    public final String processor_class;
    public final SimpleConfig processor_parameters;

    HTTPProcessorConfig(JSONObject src, HTTPProcessorConfigMap processors) throws URISyntaxException {
        super(src.getString(NAME), processors);
        enable = src.optBoolean(ENABLE, true);
        processor_class = src.getString(PROCESSOR_CLASS);
        processor_parameters = new SimpleConfig(src.optJSONObject(PROCESSOR_PARAMETERS), PROCESSOR_PARAMETERS, this);
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
