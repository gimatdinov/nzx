package cxc.jex.postprocessing;

import cxc.jex.buffer.ByteBuffer;

public abstract class Tank {
    private ByteBuffer buffer;

    public ByteBuffer getBuffer() {
        return buffer;
    }

    void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

}
