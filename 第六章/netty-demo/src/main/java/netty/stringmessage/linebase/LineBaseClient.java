package netty.stringmessage.linebase;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


/**
 * @author liyebing created on 16/2/20.
 * @version $Id$
 */
public class LineBaseClient {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new LineBaseClient().connect(port, "127.0.0.1");
    }

    public void connect(int port, String host) throws Exception {
        //创建客户端处理I/O读写的NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建客户端辅助启动类
            Bootstrap b = new Bootstrap();
            //设置NIO线程组
            b.group(group)
                    //设置NioSocketChannel,对应与JDK NIO类SocketChannel类
                    .channel(NioSocketChannel.class)
                    //设置TCP参数TCP_NODELAY
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            //设置LineBasedFrameDecoder处理器
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            //设置StringDecoder处理器
                            ch.pipeline().addLast(new StringDecoder());
                            //配置客户端处理网络I/O事件的类
                            ch.pipeline().addLast(new LineBaseClientHandler());
                        }
                    });

            // 发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();

            //循环发送1000次
            for (int i = 0; i < 1000; i++) {
                //构造客户端发送的数据ByteBuf对象
                String content = "你好,Netty!\n";
                byte[] req = content.getBytes();
                ByteBuf messageBuffer = Unpooled.buffer(req.length);
                messageBuffer.writeBytes(req);

                //向服务端发送数据
                ChannelFuture channelFuture = f.channel().writeAndFlush(messageBuffer);
                channelFuture.syncUninterruptibly();
            }
            // 等待客户端链路关闭
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 优雅退出,释放NIO线程组
            group.shutdownGracefully();
        }
    }
}
