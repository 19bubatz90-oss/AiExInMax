package com.ghostmax;
import java.util.Date;
public class ChatMessage {
    public static final int TYPE_USER = 0, TYPE_ASSISTANT = 1, TYPE_SYSTEM = 2;
    public int type; public String text; public boolean isError; public Date timestamp;
    public ChatMessage(int type, String text, boolean isError) {
        this.type = type; this.text = text; this.isError = isError; this.timestamp = new Date();
    }
    public ChatMessage(int type, String text) { this(type, text, false); }
}
