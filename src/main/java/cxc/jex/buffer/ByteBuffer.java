/** The copyright of the algorithm belong to Gimatdinov Ildar, 2016
 * 
 */
package cxc.jex.buffer;

import java.io.IOException;
import java.io.OutputStream;

public class ByteBuffer {
    private final ByteBufferPool pool;
    private final Cell cell;
    private int contentLength;
    private boolean released;

    ByteBuffer(ByteBufferPool pool, Cell cell) {
        this.pool = pool;
        this.cell = cell;
        this.contentLength = 0;
    }

    public int capacity() {
        return cell.size();
    }

    public void write(byte[] src) throws IllegalAccessException {
        if (released) {
            throw new IllegalAccessException();
        }
        if (src.length > capacity()) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(src, 0, cell.space, cell.begin, src.length);
        contentLength = src.length;
    }

    public void read(byte[] dst) throws IllegalAccessException {
        if (released) {
            throw new IllegalAccessException();
        }
        if (dst.length < contentLength) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(cell, cell.begin, dst, 0, dst.length);
    }

    public void read(OutputStream dst) throws IllegalAccessException, IOException {
        if (released) {
            throw new IllegalAccessException();
        }
        dst.write(cell.space, cell.begin, contentLength);
    }

    public void release() {
        pool.returnCell(cell);
        released = true;
    }

    public void writeByte(byte value) {
        if (contentLength == capacity()) {
            throw new IndexOutOfBoundsException();
        }
        cell.space[cell.begin + contentLength] = value;
        contentLength++;
    }

    public int getContentLength() {
        return contentLength;
    }

}
