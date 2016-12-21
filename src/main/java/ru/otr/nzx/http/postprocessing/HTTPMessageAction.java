package ru.otr.nzx.http.postprocessing;

import cxc.jex.postprocessing.PostProcessor.Action;
import ru.otr.nzx.config.http.postprocessing.ActionConfig;

public abstract class HTTPMessageAction implements Action<HTTPMessageTank> {

    protected ActionConfig config;

    public void setConfig(ActionConfig config) {
        this.config = config;
    }

    public ActionConfig getConfig() {
        return config;
    }

    public abstract void applyParameters() throws Exception;

    @Override
    public boolean isEnable() {
        return config.enable;
    }

}
