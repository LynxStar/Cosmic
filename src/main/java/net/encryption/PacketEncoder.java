package net.encryption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private final MapleAESOFB sendCypher;
    private static final Logger log = LoggerFactory.getLogger(MapleAESOFB.class);

    public PacketEncoder(MapleAESOFB sendCypher) {
        this.sendCypher = sendCypher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet in, ByteBuf out) {
        byte[] packet = in.getBytes();

        if(log.isDebugEnabled() && false)
        {
            sendCypher.DumpTrace();
            log.debug("OutboundPacket: {}", HexTool.toHexString(packet));
        }

        out.writeBytes(getEncodedHeader(packet.length));

        MapleCustomEncryption.encryptData(packet);
        sendCypher.crypt(packet);
        out.writeBytes(packet);

        if(log.isDebugEnabled() && false)
        {
            log.debug("TCPPacket: {}", HexTool.toHexString(packet));
        }
    }

    private byte[] getEncodedHeader(int length) {
        return sendCypher.getPacketHeader(length);
    }
}
