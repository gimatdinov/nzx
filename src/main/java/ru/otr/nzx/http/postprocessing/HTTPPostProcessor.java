package ru.otr.nzx.http.postprocessing;

import java.util.List;
import java.util.Map;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.http.postprocessing.ActionConfig;
import ru.otr.nzx.config.http.postprocessing.PostProcessorConfig;
import ru.otr.nzx.http.processing.HTTPProcessor;

public class HTTPPostProcessor extends PostProcessor<HTTPMessageTank> {

    private final PostProcessorConfig config;
    private final Map<String, HTTPProcessor> processors;

    public HTTPPostProcessor(PostProcessorConfig config, Map<String, HTTPProcessor> processors, Tracer tracer) {
        super(tracer);
        this.config = config;
        this.processors = processors;
    }

    public void bootstrap() {
        super.init(config.workers, config.buffer_pool_size, config.buffer_size_min, null, new ThreadFactoryBuilder().setNameFormat("PP-W-%d").build());
        try {
            for (ActionConfig cfg : config.actions.values()) {
                if (cfg.action_class != null) {
                    @SuppressWarnings("unchecked")
                    Class<HTTPMessageAction> actionClass = (Class<HTTPMessageAction>) Class.forName(cfg.action_class);
                    HTTPMessageAction action = actionClass.newInstance();
                    action.setConfig(cfg);
                    actions.add(action);
                }
                if (cfg.processor_name != null) {
                    HTTPProcessor processor = processors.get(cfg.processor_name);
                    if (processor == null) {
                        throw new RuntimeException("HTTPProcessor with name \"" + cfg.processor_name + "\" not found, need for postprocessing action [" + cfg.getName() + "]");
                    }
                    actions.add(processor.makeAction(cfg));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Action<HTTPMessageTank>> getActions() {
        return actions;
    }
}
