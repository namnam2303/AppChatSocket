
package Entity;

public class Message {
    private String senderName;
    private String recipientName;
    private String dateSend;
    private String content;  

    public Message(String senderName, String recipientName, String dateSend, String content) {
        this.senderName = senderName;
        this.recipientName = recipientName;
        this.dateSend = dateSend;
        this.content = content;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getDateSend() {
        return dateSend;
    }

    public void setDateSend(String dateSend) {
        this.dateSend = dateSend;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
