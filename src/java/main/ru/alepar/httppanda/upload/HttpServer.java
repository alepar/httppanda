package ru.alepar.httppanda.upload;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import ru.alepar.httppanda.buffer.FileByteChannelFactory;
import ru.alepar.httppanda.upload.netty.NettyBufferChannelServer;

import java.io.File;

public class HttpServer {

    public static void main(String[] args) {
        final HttpHeaders headers = new DefaultHttpHeaders();
        headers.set("Server", "Vuze Media Server 1.0");
        headers.set("Accept-Ranges", "bytes");
        headers.set("Connection", "Close");
        headers.set("Cache-Control", "no-cache");
        headers.set("Expires", "0");
        headers.set("contentFeatures.dlna.org", "DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=01;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01700000000000000000000000000000");
        headers.set("transferMode.dlna.org", "Streaming");
        headers.set("Content-Type", "video/x-matroska");

        final BufferChannelServer server = new NettyBufferChannelServer(
                new NioEventLoopGroup(8),
                new FileByteChannelFactory(new File("video.mkv")),
                31337,
                headers
        );
    }
}
