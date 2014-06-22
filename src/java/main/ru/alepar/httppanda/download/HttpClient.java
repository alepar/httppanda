package ru.alepar.httppanda.download;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import ru.alepar.httppanda.buffer.FileBufferChannel;

import java.io.File;
import java.net.URI;

public class HttpClient {

    public static void main(String[] args) throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            final File file = new File("video_far2.mkv");
            new FileDownloader(group).downloadFile(
                    new URI("http://80.251.126.234:45707/Content/6262015F2593C8B49B8FD7760EAAEA02AC959925-0.avi"),
                    20, new FileBufferChannel(file)
            );
        } finally {
            group.shutdownGracefully();
        }
    }

}
