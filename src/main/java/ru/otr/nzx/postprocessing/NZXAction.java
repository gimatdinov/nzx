package ru.otr.nzx.postprocessing;

import cxc.jex.postprocessing.Action;
import ru.otr.nzx.config.postprocessing.ActionConfig;

public abstract class NZXAction implements Action<NZXTank> {

    protected ActionConfig config;

    public void setConfig(ActionConfig config) {
        this.config = config;
    }

    public ActionConfig getConfig() {
        return config;
    }

    @Override
    public boolean isEnable() {
        return config.enable;
    }

}
