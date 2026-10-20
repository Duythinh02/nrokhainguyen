package nro.models.network;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.IOException;
import nro.models.interfaces.IMessageHandler;
import nro.models.interfaces.IMessageSendCollect;
import nro.models.interfaces.ISession;

public final class Collector
        implements Runnable {

    private ISession session;
    private DataInputStream dis;
    private IMessageSendCollect collect;
    private IMessageHandler messageHandler;

    public Collector(ISession session, Socket socket) {
        this.session = session;
        this.setSocket(socket);
    }

    public Collector setSocket(Socket socket) {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
        } catch (IOException iOException) {
        }
        return this;
    }

    @Override
    public void run() {
        try {
            while (this.session != null && this.session.isConnected()) {
                Message msg = this.collect.readMessage(this.session, this.dis);
                if (msg.command == -27) {
                    this.session.sendKey();
                } else {
                    this.messageHandler.onMessage(this.session, msg);
                }
                msg.cleanup();
            }
        } catch (IOException closed) {
            // client mất kết nối (tự thoát, tắt game, rớt mạng; Windows báo "connection was aborted/reset"):
            // bình thường, không phải lỗi gói tin
        } catch (Exception exception) {
            nro.models.utils.Logger.error("[Mạng] Ngắt " + who(this.session) + " do lỗi đọc gói tin: " + exception + "\n");
        }
        try {
            Network.gI().getAcceptHandler().sessionDisconnect(this.session);
        } catch (Exception exception) {
        }
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    // Tên nhân vật của phiên (để ghi log), không có thì trả về IP
    static String who(ISession session) {
        try {
            if (session instanceof MySession ms) {
                if (ms.player != null) {
                    return ms.player.name;
                }
                return ms.ipAddress;
            }
        } catch (Exception ignored) {
        }
        return "?";
    }

    public void setCollect(IMessageSendCollect collect) {
        this.collect = collect;
    }

    public void setMessageHandler(IMessageHandler handler) {
        this.messageHandler = handler;
    }

    public void close() {
        if (this.dis != null) {
            try {
                this.dis.close();
            } catch (IOException iOException) {
            }
        }
    }

    public void dispose() {
        this.session = null;
        this.dis = null;
        this.collect = null;
    }
}
