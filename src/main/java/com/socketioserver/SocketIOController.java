package com.socketioserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class SocketIOController {

    @Autowired
    private ISocketIOService socketIOService;

    @PostMapping("")
    public ResponseEntity<String> pushMessageToRoom(@RequestParam String roomId, @RequestParam String msgContent) {
        socketIOService.pushMessageToRoom(roomId, msgContent);
        return ResponseEntity.ok(msgContent);
    }

}
