package ru.alepar.httppanda.upload;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alepar.httppanda.buffer.FileByteChannelFactory;
import ru.alepar.httppanda.upload.netty.NettyBufferChannelServer;

import java.io.File;

public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

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

        final NioEventLoopGroup group = new NioEventLoopGroup(2);
        try {
            final BufferChannelServer server = new NettyBufferChannelServer(
                    group,
                    new FileByteChannelFactory(new File("video.mkv")),
                    31337,
                    headers
            );
        } catch (Exception e) {
            log.error("failed to start", e);
            group.shutdownGracefully();
        }
    }
}
