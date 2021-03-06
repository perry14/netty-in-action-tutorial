package com.perry14.chapter7;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class LineBasedFrameDecoderClient {
	public static void main(String[] args) throws Exception {
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap(); // (1)
			b.group(workerGroup); // (2)
			b.channel(NioSocketChannel.class); // (3)
			b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					// ch.pipeline().addLast(new LineEncoder());自己添加换行符，不使用LineEncoder
					ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
						// 在于server建立连接后，即发送请求报文
						@Override
						public void channelActive(ChannelHandlerContext ctx) {
							byte[] req1 = ("hello1" + System.getProperty("line.separator")).getBytes();
							byte[] req2 = ("hello2" + System.getProperty("line.separator")).getBytes();
							byte[] req3_1 = ("hello3").getBytes();
							byte[] req3_2 = (System.getProperty("line.separator")).getBytes();
							ByteBuf buffer = Unpooled.buffer();
							buffer.writeBytes(req1);
							buffer.writeBytes(req2);
							buffer.writeBytes(req3_1);
							ctx.writeAndFlush(buffer);
							try {
								TimeUnit.SECONDS.sleep(2);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							buffer = Unpooled.buffer();
							buffer.writeBytes(req3_2);
							ctx.writeAndFlush(buffer);
						}
					});
				}
			});
			// Start the client.
			ChannelFuture f = b.connect("127.0.0.1", 8080).sync(); // (5)
			// Wait until the connection is closed.
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
}