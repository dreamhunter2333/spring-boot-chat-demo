package com.dreamhunter.chat.model;

import lombok.Data;
import java.io.Serializable;
import javax.validation.constraints.NotNull;

@Data
public class Message implements Serializable {

    @NotNull(message = "targetId 不能为空")
    private String targetId;

    @NotNull(message = "messageText 不能为空")
    private String messageText;

    @NotNull(message = "userId 不能为空")
    private String userId;
}