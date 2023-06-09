package com.socketioserver;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service(value = "socketIOService")
public class SocketIOServiceImpl implements ISocketIOService {

    /**
     * 存放已连接的客户端
     */
    private static Map<String, SocketIOClient> clientMap = new ConcurrentHashMap<>();

    /**
     * 自定义事件`push_data_event`,用于服务端与客户端通信
     */
    private static final String PUSH_DATA_EVENT = "push_data_event";
    private static final String ROOM_ID_PARAM = "roomId";
    private static final String USER_ID_PARAM = "userId";

    @Autowired
    private SocketIOServer socketIOServer;

    /**
     * Spring IoC容器创建之后，在加载SocketIOServiceImpl Bean之后启动
     */
    @PostConstruct
    private void autoStartup() {
        start();
    }

    /**
     * Spring IoC容器在销毁SocketIOServiceImpl Bean之前关闭,避免重启项目服务端口占用问题
     */
    @PreDestroy
    private void autoStop() {
        stop();
    }

    @Override
    public void start() {
        // 监听客户端连接
        socketIOServer.addConnectListener(client -> {
            log.info("************ 客户端： " + client.getRemoteAddress() + " 已连接 ************");
            // 自定义事件`connected` -> 与客户端通信  （也可以使用内置事件，如：Socket.EVENT_CONNECT）
            String roomId = getParamsByClient(client, ROOM_ID_PARAM);
            client.joinRoom(roomId);
            client.sendEvent("connected", "你成功连接上了哦, 房间人数："+client.getCurrentRoomSize(roomId));

            socketIOServer.getRoomOperations(roomId).sendEvent("join", client.getRemoteAddress() + " join room");

            String userId = getParamsByClient(client,USER_ID_PARAM);
            if (userId != null) {
                clientMap.put(userId, client);
            }
        });

        // 监听客户端断开连接
        socketIOServer.addDisconnectListener(client -> {
            String clientIp = client.getRemoteAddress().toString();
            log.info(clientIp + " *********************** " + "客户端已断开连接");

            String roomId = getParamsByClient(client, ROOM_ID_PARAM);
            client.leaveRoom(roomId);
            socketIOServer.getRoomOperations(roomId).sendEvent("leave",  clientIp +" leave room");

            String userId = getParamsByClient(client,USER_ID_PARAM);
            if (userId != null) {
                clientMap.remove(userId);
                client.disconnect();
            }
        });

        // 自定义事件`push_data_event` -> 监听客户端消息
        socketIOServer.addEventListener(PUSH_DATA_EVENT, Packet.class, (client, data, ackSender) -> {
            // 客户端推送`push_data_event`事件时，onData接受数据，这里是string类型的json数据，还可以为Byte[],object其他类型
            String clientIp = getIpByClient(client);
            String roomId = getParamsByClient(client, ROOM_ID_PARAM);

            socketIOServer.getRoomOperations(roomId).sendEvent("message", clientIp + " :  " + data.getData());
            log.info(clientIp + " ************ 客户端：" + data.getData());
        });

        // 启动服务
        socketIOServer.start();

    }

    @Override
    public void stop() {
        if (socketIOServer != null) {
            socketIOServer.stop();
            socketIOServer = null;
        }
    }

    @Override
    public void pushMessageToRoom(String roomId, String msgContent) {
        socketIOServer.getRoomOperations(roomId).sendEvent("message", msgContent);
    }

    /**
     * 获取客户端url中的userId参数（这里根据个人需求和客户端对应修改即可）
     *
     * @param client: 客户端
     * @return: java.lang.String
     */
    private String getParamsByClient(SocketIOClient client, String param) {
        // 获取客户端url参数（这里的userId是唯一标识）
        Map<String, List<String>> params = client.getHandshakeData().getUrlParams();
        List<String> paramList = params.get(param);
        if (!CollectionUtils.isEmpty(paramList)) {
            return paramList.get(0);
        }
        return null;
    }

    /**
     * 获取连接的客户端ip地址
     *
     * @param client: 客户端
     * @return: java.lang.String
     */
    private String getIpByClient(SocketIOClient client) {
        String sa = client.getRemoteAddress().toString();
        return sa.substring(1, sa.indexOf(":"));
    }
}
