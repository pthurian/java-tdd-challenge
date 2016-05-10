package phones.impl;

import phones.PhoneStatus;
import phones.system.Call;
import phones.system.CallIncoming;
import phones.system.CallOutgoing;
import phones.system.PhoneSocket;
import phones.system.ConnectedPhone;
import phones.system.RejectReason;

/**
 *
 */
public class DefaultPhone implements ConnectedPhone {
    
    private final PhoneSocket socket;
    private PhoneStatus status = PhoneStatus.IDLE;
    private String lastMessage = null;
    private CallOutgoing outgoing = null;
    private CallIncoming incoming = null;
    private Call call = null;

    public DefaultPhone(PhoneSocket socket) {
        this.socket = socket;
    }

    @Override
    public String getNumber() {
        return socket.getNumber();
    }

    @Override
    public void dial(String number) {
        outgoing = socket.call(number, this::onCallReject, this::onCallAccept, this::onMessageReceive, this::onCallEnd);
    }

    @Override
    public void pushGreen() {
        if (status == PhoneStatus.RINGING) {
            call = incoming.accept(this::onMessageReceive, this::onCallEnd);
            this.onCallAccept(call);
        } else {
            status = PhoneStatus.CALLING;
        }
    }

    @Override
    public void pushRed() {
        if (status == PhoneStatus.RINGING) {
            incoming.reject(RejectReason.BUSY);
        } else if (status == PhoneStatus.IN_CALL) {
            call.end();
        }
    }

    @Override
    public void send(String message) {
        call.send(message);
    }

    @Override
    public void receive(CallIncoming request) {
        incoming = request;
        //call = incoming.accept(this::onMessageReceive, this::onCallEnd);
        status = PhoneStatus.RINGING;
    }

    @Override
    public void canceled(CallIncoming request) {
        status = PhoneStatus.IDLE;
    }
    
    protected void onCallReject(RejectReason reason) {
        status = PhoneStatus.IDLE;
    }
    
    protected void onCallAccept(Call call) {
        this.call = call;
        status = PhoneStatus.IN_CALL;
    }
    
    protected void onMessageReceive(String message) {
        lastMessage = message;
    }
    
    protected void onCallEnd() {
        status = PhoneStatus.IDLE;
    }
    
    @Override
    public PhoneStatus getStatus() {
        return status;
    }

    @Override
    public String getLastMessage() {
        String m = lastMessage;
        lastMessage = null;
        return m;
    }

    @Override
    public String toString() {
        return "Phone[" + getNumber() + "]";
    }
}
