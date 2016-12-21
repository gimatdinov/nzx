package ru.otr.nzx.http.postprocessing;

import ru.otr.nzx.config.model.ActionConfig;

public abstract class NZXAction implements cxc.jex.postprocessing.Action<NZXTank> {
    protected final ActionConfig config;

    public NZXAction(ActionConfig config) {
        this.config = config;
    }

    public abstract void applyParameters() throws Exception;

    @Override
    public boolean isEnable() {
        return config.enable;
    }

}
