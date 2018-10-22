package org.j316.cellserver.view.binding;

import java.util.Date;

public class ServerStatus {

    Date timestamp = new Date();
    String status;

    public ServerStatus(String status) {
        this.status = status;
    }


    public Date getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }
}

