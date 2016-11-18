package ru.otr.nzx.postprocessing;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cxc.jex.postprocessing.Action;
import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.postprocessing.Tank;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.postprocessing.ActionConfig;
import ru.otr.nzx.config.postprocessing.PostProcessorConfig;

public class NZXPostProcessor extends PostProcessor {

    private final PostProcessorConfig config;
    private final int bufferSize;

    public NZXPostProcessor(String name, PostProcessorConfig config, int bufferSize, Tracer tracer) {
        super(name, tracer);
        this.config = config;
        this.bufferSize = bufferSize;
    }

    @Override
    protected Tank makeTank() {
        return new NZXTank(bufferSize);
    }

    private static List<Action> loadActions(List<ActionConfig> configs) {
        try {
            List<Action> result = new ArrayList<>();
            result.add(new Dumping());
            for (ActionConfig cfg : configs) {
                Class<?> actionClass = Class.forName(cfg.clazz);
                Class<?>[] paramTypes = new Class[cfg.parameters.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    paramTypes[i] = String.class;
                }
                Constructor<?> actionConstructor = actionClass.getConstructor(paramTypes);
                result.add((Action) actionConstructor.newInstance((Object[]) cfg.parameters));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bootstrap() {
        super.init(config.workers, config.max_count_of_tanks, loadActions(config.actions),
                new ThreadFactoryBuilder().setNameFormat("nzx-PostProcessor-Worker-%d").build());
    }

    public boolean isDumpingEnable() {
        for (Action action : actions) {
            if (action instanceof Dumping) {
                return true;
            }
        }
        return false;
    }

}
