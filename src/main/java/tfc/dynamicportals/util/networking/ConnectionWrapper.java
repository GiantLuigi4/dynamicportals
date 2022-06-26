package tfc.dynamicportals.util.networking;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;
import tfc.dynamicportals.access.ConnectionAccessor;

import javax.crypto.Cipher;
import java.net.SocketAddress;
import java.util.ArrayList;

public class ConnectionWrapper extends Connection {
	public ArrayList<PacketProcessor> processors = new ArrayList<>();
	Connection parent;
	
	public ConnectionWrapper(Connection parent) {
		super(parent.getSending());
		this.parent = parent;
	}
	
	@Override
	public void send(Packet<?> pPacket) {
		super.send(pPacket);
	}
	
	@Override
	public void send(Packet<?> p_129515_, @Nullable GenericFutureListener<? extends Future<? super Void>> p_129516_) {
		WrapperPacket packet = new WrapperPacket(p_129515_);
		packet.processors.addAll(processors);
		if (packet.processors.isEmpty()) parent.send(p_129515_, p_129516_);
		else parent.send(packet, p_129516_);
	}
	
	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
		return parent.acceptInboundMessage(msg);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		parent.channelRead(ctx, msg);
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		parent.channelRegistered(ctx);
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		parent.channelUnregistered(ctx);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		parent.channelReadComplete(ctx);
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		parent.userEventTriggered(ctx, evt);
	}
	
	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		parent.channelWritabilityChanged(ctx);
	}
	
	@Override
	protected void ensureNotSharable() {
		super.ensureNotSharable();
	}
	
	@Override
	public boolean isSharable() {
		return parent.isSharable();
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		parent.handlerAdded(ctx);
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		parent.handlerRemoved(ctx);
	}
	
	@Override
	public boolean equals(Object o) {
		return parent.equals(o);
	}
	
	@Override
	public int hashCode() {
		return parent.hashCode();
	}
	
	@Override
	public String toString() {
		return parent.toString();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext p_129525_) throws Exception {
		parent.channelActive(p_129525_);
	}
	
	@Override
	public void setProtocol(ConnectionProtocol pNewState) {
		parent.setProtocol(pNewState);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext p_129527_) {
		parent.channelInactive(p_129527_);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext p_129533_, Throwable p_129534_) {
		parent.exceptionCaught(p_129533_, p_129534_);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext p_129487_, Packet<?> p_129488_) {
		try {
			((ConnectionAccessor) parent).invokeRead0(p_129487_, p_129488_);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setListener(PacketListener pHandler) {
		parent.setListener(pHandler);
	}
	
	@Override
	public void tick() {
//		parent.tick();
	}
	
	@Override
	protected void tickSecond() {
//		((ConnectionAccessor) parent).invokeTickSecond();
	}
	
	@Override
	public SocketAddress getRemoteAddress() {
		return parent.getRemoteAddress();
	}
	
	@Override
	public void disconnect(Component pMessage) {
		parent.disconnect(pMessage);
	}
	
	@Override
	public boolean isMemoryConnection() {
		return parent.isMemoryConnection();
	}
	
	@Override
	public PacketFlow getReceiving() {
		return parent.getReceiving();
	}
	
	@Override
	public PacketFlow getSending() {
		return parent.getSending();
	}
	
	@Override
	public void setEncryptionKey(Cipher pDecryptingCipher, Cipher pEncryptingCipher) {
		parent.setEncryptionKey(pDecryptingCipher, pEncryptingCipher);
	}
	
	@Override
	public boolean isEncrypted() {
		return parent.isEncrypted();
	}
	
	@Override
	public boolean isConnected() {
		return parent.isConnected();
	}
	
	@Override
	public boolean isConnecting() {
		return parent.isConnecting();
	}
	
	@Override
	public PacketListener getPacketListener() {
		return parent.getPacketListener();
	}
	
	@Nullable
	@Override
	public Component getDisconnectedReason() {
		return parent.getDisconnectedReason();
	}
	
	@Override
	public void setReadOnly() {
		parent.setReadOnly();
	}
	
	@Override
	public void setupCompression(int pThreshold, boolean pValidateDecompressed) {
		parent.setupCompression(pThreshold, pValidateDecompressed);
	}
	
	@Override
	public void handleDisconnection() {
		parent.handleDisconnection();
	}
	
	@Override
	public float getAverageReceivedPackets() {
		return parent.getAverageReceivedPackets();
	}
	
	@Override
	public float getAverageSentPackets() {
		return parent.getAverageSentPackets();
	}
	
	@Override
	public Channel channel() {
		return parent.channel();
	}
	
	@Override
	public PacketFlow getDirection() {
		return parent.getDirection();
	}
}
