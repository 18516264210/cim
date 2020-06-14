package com.crossoverjie.cim.handle;

import com.crossoverjie.cim.common.constant.Constants;
import com.crossoverjie.cim.common.protocol.CIMRequestProto;
import com.crossoverjie.cim.common.protocol.CIMResponseProto;
import com.crossoverjie.cim.common.util.NettyAttrUtil;
import com.crossoverjie.cim.common.util.SpringBeanFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 16/02/2018 18:09
 * @since JDK 1.8
 */
@ChannelHandler.Sharable
public class CIMClientHandle extends SimpleChannelInboundHandler<CIMResponseProto.CIMResProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(CIMClientHandle.class);


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt ;

            if (idleStateEvent.state() == IdleState.WRITER_IDLE){
                CIMRequestProto.CIMReqProtocol heartBeat = SpringBeanFactory.getBean("heartBeat",
                        CIMRequestProto.CIMReqProtocol.class);
                ctx.writeAndFlush(heartBeat).addListeners((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        LOGGER.error("IO error,close Channel");
                        future.channel().close();
                    }
                }) ;
            }

        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端和服务端建立连接时调用
        LOGGER.info("cim server connect success!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CIMResponseProto.CIMResProtocol msg) throws Exception {

        //心跳更新时间
        if (msg.getType() == Constants.CommandType.PING){
            NettyAttrUtil.updateReaderTime(ctx.channel(),System.currentTimeMillis());
        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常时断开连接
        cause.printStackTrace() ;
        ctx.close() ;
    }
}
