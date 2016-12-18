package ru.otr.nzx.postprocessing;

import java.io.File;
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

    public void bootstrap() {
        super.init(config.workers, config.buffer_pool_size, config.buffer_size_min, null, new ThreadFactoryBuilder().setNameFormat("PP-W-%d").build());

        File store = new File(config.dumps_store).getAbsoluteFile();
        if (!store.exists() && !store.mkdirs()) {
            throw new RuntimeException("Cannot make directory [" + store.getPath() + "]");
        }
        actions.add(new Dumping(config.dumps_store));
        try {
            for (ActionConfig cfg : config.actions.values()) {
                @SuppressWarnings("unchecked")
                Class<NZXAction> actionClass = (Class<NZXAction>) Class.forName(cfg.action_class);
                NZXAction action = actionClass.newInstance();
                action.setConfig(cfg);
                actions.add(action);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDumpingEnable() {
        for (Action<NZXTank> action : actions) {
            if (action instanceof Dumping) {
                return true;
            }
        }
        return false;
    }

    public boolean isDumpingAll() {
        return config.dumping_all;
    }

    public List<Action<NZXTank>> getActions() {
        return actions;
    }
}
