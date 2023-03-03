package com.github.cheukbinli.core.ns.command.service;

import com.github.cheukbinli.core.ns.command.ConnectionApi;
import com.github.cheukbinli.core.ns.command.impl.CommandApi;
import com.github.cheukbinli.core.ns.command.impl.DefaultCommandApi;
import com.github.cheukbinli.core.ns.command.impl.WirelessConnectionApi;
import lombok.Data;

import java.io.IOException;

@Data
public class CommandService {

    CommandApi commandApi;
    ConnectionApi connectionApi;

    private final String ipAddr;
    private final int port;

    public CommandService(String ipAddr, int port) throws IOException {
        this.ipAddr = ipAddr;
        this.port = port;
        start();
    }

    public synchronized void start() throws IOException {
        if (null == commandApi) {
            connectionApi = new WirelessConnectionApi(ipAddr, port);
            commandApi = new DefaultCommandApi(connectionApi);
        }
    }

}
