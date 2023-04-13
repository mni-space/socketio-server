package com.socketioserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socket.io")
public class SocketIOController {

    @Autowired
    private ISocketIOService socketIOService;

    @PostMapping("/pushMessageToUser")
    public ResponseEntity<String> pushMessageToUser(@RequestParam String userId, @RequestParam String msgContent) {
        socketIOService.pushMessageToUser(userId, msgContent);
        return ResponseEntity.ok(msgContent);
    }

}
