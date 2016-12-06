/** The copyright of the algorithm belong to Gimatdinov Ildar, 2016
 * 
 */
package cxc.jex.buffer;

import java.util.concurrent.atomic.AtomicInteger;

class Cell {
    final byte[] space;
    final int begin;
    final int end;

    Cell host;
    Cell first;
    Cell second;

    private final int size;
    private final AtomicInteger freeSpace;

    public Cell(byte[] space, int begin, int end) {
        this.space = space;
        this.begin = begin;
        this.end = end;
        this.size = end - begin + 1;
        this.freeSpace = new AtomicInteger(size);
    }

    private void holdUp(int len) {
        if (host != null) {
            int hfs;
            do {
                hfs = host.freeSpace.get();
            } while (!host.freeSpace.compareAndSet(hfs, hfs - len));
            host.holdUp(len);
        }
    }

    private void unholdUp(int len) {
        if (host != null) {
            int hfs;
            do {
                hfs = host.freeSpace.get();
            } while (!host.freeSpace.compareAndSet(hfs, hfs + len));
            host.unholdUp(len);
        }
    }

    void hold() {
        if (isBusy()) {
            throw new IllegalStateException();
        }
        freeSpace.set(0);
        holdUp(size);
        holdDown();
    }

    void unhold() {
        freeSpace.set(size);
        unholdUp(size);
        unholdDown();
    }

    private void holdDown() {
        if (first != null) {
            first.freeSpace.set(0);
            first.holdDown();
            second.freeSpace.set(0);
            second.holdDown();
        }
    }

    private void unholdDown() {
        if (first != null) {
            first.freeSpace.set(freeSpace.get() / 2);
            first.unholdDown();
            second.freeSpace.set(freeSpace.get() / 2);
            second.unholdDown();
        }
    }

    public boolean isBusy() {
        return freeSpace.get() < size;
    }

    public int getFreeSpace() {
        return freeSpace.get();
    }

    public int size() {
        return size;
    }

}
