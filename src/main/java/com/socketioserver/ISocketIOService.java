package com.socketioserver;

public interface ISocketIOService {
    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务
     */
    void stop();

    /**
     * 推送信息给指定客户端
     *
     * @param roomId:     房间客户端唯一标识
     * @param msgContent: 消息内容
     */
    void pushMessageToRoom(String roomId, String msgContent);
}
