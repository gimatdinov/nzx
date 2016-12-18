package ru.otr.nzx.extra.dumpsearch;

import cxc.jex.postprocessing.Action;
import cxc.jex.tracer.Tracer;
import ru.otr.nzx.postprocessing.Dumping;
import ru.otr.nzx.postprocessing.NZXTank;

public class DumpIndexing implements Action<NZXTank> {

    private final DumpSearchProcessor processor;

    public DumpIndexing(DumpSearchProcessor processor) {
        this.processor = processor;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void process(NZXTank tank, Tracer tracer) throws Exception {
        if (Dumping.isProcess(tank)) {
            tracer.debug("DumpIndexing", Dumping.makePath(tank));
            processor.indexDump(tank);
        }
    }

}
