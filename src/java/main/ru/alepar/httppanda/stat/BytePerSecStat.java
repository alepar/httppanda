package ru.alepar.httppanda.stat;

import java.util.LinkedList;
import java.util.List;

public class BytePerSecStat implements IoStat {

    private final List<Node> nodes = new LinkedList<>();

    private long total;

    @Override
    public void add(long length) {
        final long curTime = System.nanoTime();

        nodes.add(new Node(curTime, length));
        total += length;

        while (curTime - nodes.get(0).nanoTime > 10_000_000_000L) {
            total -= nodes.remove(0).length;
        }
    }

    @Override
    public double get() {
        final long start = nodes.get(0).nanoTime;
        final long end = nodes.get(nodes.size()-1).nanoTime;

        return ((double)total) * 1_000_000_000.0 / (end-start);
    }

    private static class Node {
        private final long nanoTime;
        private final long length;

        private Node(long nanoTime, long length) {
            this.nanoTime = nanoTime;
            this.length = length;
        }
    }
}
