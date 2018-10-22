package org.j316.cellserver.view.binding;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.context.annotation.SessionScope;

@Component
public class CellOperation {
    String sendValue;

    public CellOperation(String sendValue) {
        this.sendValue = sendValue;
    }

    public CellOperation() {
    }

    public String getSendValue() {
        return sendValue;
    }

    public void setSendValue(String sendValue) {
        this.sendValue = sendValue;
    }
}
