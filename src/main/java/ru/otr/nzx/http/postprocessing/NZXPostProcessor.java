package ru.otr.nzx.http.postprocessing;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.model.ActionConfig;
import ru.otr.nzx.config.model.PostProcessorConfig;
import ru.otr.nzx.http.processing.Processor;

public class NZXPostProcessor extends cxc.jex.postprocessing.PostProcessor<NZXTank> {
    private final PostProcessorConfig config;
    private final Map<String, Processor> processors;

    public NZXPostProcessor(PostProcessorConfig config, Map<String, Processor> processors, Tracer tracer) {
        super(null, config.workers, new ThreadFactoryBuilder().setNameFormat("PP-W-%d").build(), tracer);
        this.config = config;
        this.processors = processors;
    }

    public void bootstrap() {
        super.bootstrap(config.buffer_pool_size, config.buffer_size_min);
        try {
            for (ActionConfig cfg : config.actions.values()) {
                if (cfg.action_class != null) {
                    @SuppressWarnings("unchecked")
                    Class<NZXAction> clazz = (Class<NZXAction>) Class.forName(cfg.action_class);
                    Constructor<NZXAction> constructor = clazz.getConstructor(new Class[] { ActionConfig.class, });
                    NZXAction action = constructor.newInstance(cfg);
                    action.applyParameters();
                    actions.add(action);
                }
                if (cfg.processor_name != null) {
                    Processor processor = processors.get(cfg.processor_name);
                    actions.add(processor.makeAction(cfg));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
