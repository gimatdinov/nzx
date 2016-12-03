package ru.otr.nzx.postprocessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cxc.jex.postprocessing.Action;
import cxc.jex.postprocessing.PostProcessor;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.config.postprocessing.ActionConfig;
import ru.otr.nzx.config.postprocessing.PostProcessorConfig;

public class NZXPostProcessor extends PostProcessor<NZXTank> {

    private final PostProcessorConfig config;

    public NZXPostProcessor(PostProcessorConfig config, Tracer tracer) {
        super(tracer);
        this.config = config;
    }

    private static List<Action<NZXTank>> loadActions(Collection<ActionConfig> configs) {
        try {
            List<Action<NZXTank>> result = new ArrayList<>();
            result.add(new Dumping());
            for (ActionConfig cfg : configs) {
                Class<?> actionClass = Class.forName(cfg.action_class);
                NZXAction action = (NZXAction) actionClass.newInstance();
                action.setConfig(cfg);
                result.add(action);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void bootstrap() {
        super.init(config.workers, config.buffer_pool_size, config.buffer_size_min, loadActions(config.actions.values()),
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
