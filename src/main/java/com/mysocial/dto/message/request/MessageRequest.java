package com.mysocial.dto.message.request;

import com.mysocial.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class MessageRequest {
    private Long receiverId;
    private Long groupId;
    private String content;
    private Message.MessageType type;
    private Long replyToId;
    private Message.MessageStatus status;
    public MessageRequest(){
        this.receiverId = null;
        this.groupId = null;
        this.replyToId = null;
    }
    private String fileBase64; // Base64 từ phía frontend
    private String fileName;   // Tên gốc file (ví dụ: abc.png)

}
