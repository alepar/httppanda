package ru.alepar.httppanda.upload.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import ru.alepar.httppanda.buffer.BufferChannel;

import java.nio.ByteBuffer;

public class BufferChannelChunkedInput implements ChunkedInput<ByteBuf> {

    private static final int CHUNK_SIZE = 1024*1024;

    private final BufferChannel bufferChannel;
    private final long end;

    private long start;

    public BufferChannelChunkedInput(BufferChannel bufferChannel, long start, long end) {
        this.bufferChannel = bufferChannel;
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        return start > end;
    }

    @Override
    public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
        if (isEndOfInput()) {
            return null;
        }

        final int chunkSize = (int) Math.min(end - start + 1, CHUNK_SIZE);
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(chunkSize);
        bufferChannel.read(byteBuffer, start);
        start += chunkSize;
        byteBuffer.flip();
        return Unpooled.wrappedBuffer(byteBuffer);
    }

    @Override
    public void close() throws Exception { }
}
