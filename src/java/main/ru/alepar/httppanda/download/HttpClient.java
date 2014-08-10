package ru.alepar.httppanda.download;

import io.netty.channel.nio.NioEventLoopGroup;
import ru.alepar.httppanda.buffer.FileByteChannelFactory;

import java.io.File;
import java.net.URI;

public class HttpClient {

    public static void main(String[] args) throws Exception {
        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            new FileDownloader(group).downloadFile(
//                    new URI("http://80.251.126.234:45707/Content/6262015F2593C8B49B8FD7760EAAEA02AC959925-0.avi"),
                    new URI("http://127.0.0.1:31337"),
                    new FileByteChannelFactory(new File("video_down2.mkv")),
                    1
            );
        } finally {
            group.shutdownGracefully();
        }
    }

}
