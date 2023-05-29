package com.example.Service.Impl;

import cn.hutool.extra.spring.SpringUtil;
import com.example.Entity.User;
import com.example.Service.UserService;
import com.example.Util.DecodeJwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
@ServerEndpoint(value = "/websocket/{token}")
public class WebSocketServer {
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */

    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的WebSocket对象。
     */

    private static ConcurrentHashMap<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */

    private Session session;
    /**
     * 缓存的消息,因为会存在同时写消息-读消息，写消息-删除消息的情况，需要保证线程安全*
     */

    private static ConcurrentHashMap<Long, List<String>> cacheMessage = new ConcurrentHashMap<>();

    public Session getSession() {
        return session;
    }

    /**
     * 接收userId
     */
    private Long userId;

    private static UserService userService;
    @Autowired
    private void setUserService(UserService userService){
        this.userService = userService;
    };


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println(cacheMessage);
        this.session = session;
        if (token == null) {
            log.info("WebSocket token is null");
            return;
        }
        DecodeJwtUtils jwtUtils=new DecodeJwtUtils();
        String user_id=jwtUtils.getId(token);
        User user = userService.getUserById(Long.parseLong(user_id));
        this.userId = user.getId();
        if (webSocketMap.containsKey(userId)) {
            // 如果再次连接 ，丢掉上次连接 ,存储本次连接
            webSocketMap.remove(userId);
            //加入map中
            webSocketMap.put(userId, this);
        } else {
            //加入map中
            webSocketMap.put(userId, this);
            //在线数加1
            addOnlineCount();
        }
        log.info("用户连接:" + userId + ",当前在线人数为:" + getOnlineCount());
        //是否有暂存的消息，如果有则发送消息
        boolean contains = cacheMessage.containsKey(userId);
        List<String> Strings = cacheMessage.get(userId);
        if (contains) {
            //取出消息列表
            List<String> list = Strings;
            if (list == null) {
                log.info("用户" + userId + "没有缓存的消息");
            }
            list.forEach(message -> {
                sendMessage(userId, message);
            });
            log.info("用户" + userId + "缓存的消息发送成功");
            list = null;
        }
        System.out.println("缓存的消息cacheMessage" + cacheMessage);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            log.info("userId = ", userId);
            webSocketMap.remove(userId);
            //从set中删除
            //判断在线人数是否为0，避免在线人数出现负数
            if (WebSocketServer.onlineCount > 0) {
                subOnlineCount();
            }
        }
        log.info("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     **/
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("session= " + session);
        log.info("用户消息:" + userId + ",报文:" + message);
        session.getBasicRemote().sendText("websocker 已收到消息");
    }


    /**
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(Long userId, String message) {
        try {
            boolean contains = webSocketMap.containsKey(userId);
            if (contains) {
                WebSocketServer webSocketServer = webSocketMap.get(userId);
                if (webSocketServer == null) {
                    log.info("WebSocket server not found");
                    return;
                }
                webSocketServer.getSession().getBasicRemote().sendText(message);
                log.info("WebSocket 消息发送成功");
            } else {
                // 用户不在线，暂存消息
                //获取消息列表
                List<String> list = cacheMessage.get(userId);
                if (list == null) {
                    // list会存在并发修改异常，需要一个线程安全的List
                    list = new CopyOnWriteArrayList<>();
                    cacheMessage.put(userId, list);
                }
                //把新消息添加到消息列表
                list.add(message);
                log.info("消息暂存成功" + cacheMessage.get(userId).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: 实现服务器主动推送实体消息
     * @author Mr.Liud
     * @date 2022/6/11 9:49
     * @version 1.0
     */
    public void sendObjectMessage(Object message) {
        try {
            this.session.getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获得此时的在线人数
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 在线人数加1
     */
    public static synchronized void addOnlineCount() {
        System.out.println("在线人数加1");
        WebSocketServer.onlineCount++;
    }

    /**
     * 在线人数减1
     */
    public static synchronized void subOnlineCount() {
        System.out.println("在线人数减1");
        WebSocketServer.onlineCount--;
    }

}