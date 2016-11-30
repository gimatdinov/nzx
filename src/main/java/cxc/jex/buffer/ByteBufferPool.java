package cxc.jex.buffer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBufferPool {
    private static Logger log = LoggerFactory.getLogger(ByteBufferPool.class);

    private Lock lock = new ReentrantLock();
    private Condition returnedСell = lock.newCondition();

    private final int bufferSizeMin;
    final byte[] space;

    private final Cell[][] layout;

    public ByteBufferPool(int size, int bufferSizeMin) {
        this.bufferSizeMin = bufferSizeMin;
        int lg = (int) Math.round((Math.log(Math.ceil(((double) size) / bufferSizeMin)) / Math.log(2.0)));
        int _size = ((int) Math.pow(2, lg)) * bufferSizeMin;
        log.info("init " + (lg + 1) + " levels, space = " + _size);
        space = new byte[_size];
        layout = new Cell[lg + 1][];
        for (int i = 0; i < layout.length; i++) {
            layout[i] = new Cell[(int) Math.pow(2, layout.length - i - 1)];
            for (int j = 0; j < layout[i].length; j++) {
                int cv = bufferSizeMin * (int) Math.pow(2, i);
                layout[i][j] = new Cell(space, j * cv, (j + 1) * cv - 1);
            }
        }
        if (layout.length > 2) {
            layout[layout.length - 1][0].first = layout[layout.length - 2][0];
            layout[layout.length - 1][0].second = layout[layout.length - 2][1];
            for (int i = 0; i < layout.length - 1; i++) {
                for (int j = 0; j < layout[i].length; j++) {
                    layout[i][j].host = layout[i + 1][j / 2];
                    if (j % 2 == 0) {
                        layout[i][j].host.first = layout[i][j];
                    } else {
                        layout[i][j].host.second = layout[i][j];
                    }
                }
            }
        }
    }

    private int getLevelFor(int capacity) {
        if (capacity > space.length) {
            return -1;
        } else {
            if (capacity < bufferSizeMin) {
                return 0;
            } else {
                return (int) Math.ceil((Math.log(Math.ceil(((double) capacity) / bufferSizeMin)) / Math.log(2.0)));
            }
        }
    }

    public ByteBuffer borrow(int capacity) {
        log.trace("free space " + layout[layout.length - 1][0].getFreeSpace());
        int level = getLevelFor(capacity);
        log.trace("borrow " + capacity + " level " + level);
        if (level < 0) {
            throw new IndexOutOfBoundsException();
        }
        Cell cell = null;
        while (cell == null) {
            lock.lock();
            try {
                int ri = (int) Math.floor((Math.random() * (layout[level].length - 1)));
                for (int i = ri; i >= 0; i--) {
                    if (!layout[level][i].isBusy()) {
                        cell = layout[level][i];
                        cell.hold();
                        break;
                    }
                }
                if (cell == null) {
                    for (int i = ri + 1; i < layout[level].length; i++) {
                        if (!layout[level][i].isBusy()) {
                            cell = layout[level][i];
                            cell.hold();
                            break;
                        }
                    }
                }
                if (cell == null) {
                    returnedСell.await();
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
        log.trace("cell size=" + cell.size() + " pos=(" + cell.begin + ", " + cell.end + ")");
        return new ByteBuffer(this, cell);
    }

    public void returnCell(Cell cell) {
        lock.lock();
        try {
            cell.unhold();
            returnedСell.signal();
        } finally {
            lock.unlock();
        }
    }

    public int getFreeSpace() {
        return layout[layout.length - 1][0].getFreeSpace();
    }

//    public static void main(String... args) {
//        ByteBufferPool pool = new ByteBufferPool(100, 9);
//        ByteBuffer buf = pool.borrow(10);
//        log.info("" + pool.getFreeSpace());
//        buf.release();
//        log.info("" + pool.getFreeSpace());
//    }

}
