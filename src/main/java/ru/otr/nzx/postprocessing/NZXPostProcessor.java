package ru.otr.nzx.postprocessing;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cxc.jex.postprocessing.Action;
import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.postprocessing.ActionConfig;
import ru.otr.nzx.config.postprocessing.PostProcessorConfig;

public class NZXPostProcessor extends PostProcessor<NZXTank> {

    private final PostProcessorConfig config;

    public NZXPostProcessor(String name, PostProcessorConfig config, Tracer tracer) {
        super(name, tracer);
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    private static List<Action<NZXTank>> loadActions(List<ActionConfig> configs) {
        try {
            List<Action<NZXTank>> result = new ArrayList<>();
            result.add(new Dumping());
            for (ActionConfig cfg : configs) {
                Class<?> actionClass = Class.forName(cfg.clazz);
                Class<?>[] paramTypes = new Class[cfg.parameters.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    paramTypes[i] = String.class;
                }
                Constructor<?> actionConstructor = actionClass.getConstructor(paramTypes);
                result.add((Action<NZXTank>) actionConstructor.newInstance((Object[]) cfg.parameters));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bootstrap() {
        super.init(config.workers, config.buffer_pool_size, config.buffer_size_min, loadActions(config.actions),
                new ThreadFactoryBuilder().setNameFormat("nzx-PostProcessor-Worker-%d").build());
    }

    public boolean isDumpingEnable() {
        for (Action<NZXTank> action : actions) {
            if (action instanceof Dumping) {
                return true;
            }
        }
        return false;
    }

}
