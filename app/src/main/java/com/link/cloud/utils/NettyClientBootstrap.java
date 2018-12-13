package com.link.cloud.utils;


import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.link.cloud.base.Constants;

import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;


/**
 * Created by 49488 on 2018/10/13.
 */
public class NettyClientBootstrap {
    private int port;
    private String host;
    public SocketChannel socketChannel;
    private NioEventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private boolean isConnect = false;
    public boolean isRepeate = false;
    public Context context;
    String msg;
    private Intent intent;
    private Object type;

    public NettyClientBootstrap(Context context, int port, String host, String msg) {
        this.context = context;
        this.port = port;
        this.msg = msg;
        this.host = host;
        //初始化连接
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(1024, 1024 * 32, 1024 * 64));
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                //超时处理
                socketChannel.pipeline().addLast(new IdleStateHandler(0, 10, 0));
                //编码，解码，回调，如果没有用带编解码删除new Encode(), new Decode()
                socketChannel.pipeline().addLast(new StringDecoder(), new StringEncoder(), new NettyClientHandler());
            }
        });
    }

    public void startNetty(final String msg) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (isConnect) {
                    socketChannel.writeAndFlush(msg);
                }
            }
        }.start();

    }


    private ChannelFuture future = null;

    public void start() {

        try {
            future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                isConnect = true;
                //成功状态监听在此处（包括重连成功状态）
                Log.e("channelRead0: ", isConnect + "");
                return;
            } else {
                Log.e("channelRead0", "future is unConnect");
            }
        } catch (Exception e) {
            Log.e("channelRead0: ", e.getMessage() + "");
        }
        //连接状态在此处处理
        repeateTcp();
    }


    public void repeateTcp() {
        if (!isRepeate) {
            if (isConnect) {
                isConnect = false;
                //断线监听在此处处理
            }
            isRepeate = true;
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("channelRead0: ", e.getMessage() + "");
            }
            isRepeate = false;
            start();
        }
    }

    public void close() {
        if (future != null && future.channel() != null) {
            if (future.channel().isOpen())
                future.channel().close();
        }
    }


    public class NettyClientHandler extends SimpleChannelInboundHandler<Object> {
        //利用写空闲发送心跳检测消息
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                switch (e.state()) {
                    case WRITER_IDLE:
                        if(isConnect){
                            startNetty(msg);
                        }
                        break;
                }
            }
        }


        //这里是接受服务端发送过来的消息
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object baseMsg) throws Exception {
            String msgObj = (String) baseMsg;
            Log.e("channelRead0: ", msgObj);
            try {
                JSONObject object = new JSONObject(msgObj);
                type = object.get("msgType");

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!"HEART_BEAT".equals(type)) {
                if (intent != null) {
                    intent.setAction(Constants.MSG);
                    intent.putExtra("msg", msgObj);
                    Log.e("channelRead0: ", "111");
                    context.sendBroadcast(intent);
                } else {
                    intent = new Intent();
                    intent.setAction(Constants.MSG);
                    intent.putExtra("msg", msgObj);
                    context.sendBroadcast(intent);
                    Log.e("channelRead0: ", "222");
                }
            }
        }


        //这里是断线要进行的操作
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Log.e("channelRead0: ", "1111");
            repeateTcp();
        }

        //这里是出现异常的话要进行的操作
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            Log.e("channelRead0", "TcpHandler--ErrorConnect--" + cause.toString());
            //断线监听在repeateTcp方法内
            repeateTcp();
        }

    }


}


