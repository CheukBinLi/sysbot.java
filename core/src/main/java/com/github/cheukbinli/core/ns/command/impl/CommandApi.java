package com.github.cheukbinli.core.ns.command.impl;

import com.github.cheukbinli.core.ns.command.BaseCommand;
import com.github.cheukbinli.core.ns.command.ConnectionApi;
import com.github.cheukbinli.core.ns.command.ControllerCommand;
import com.github.cheukbinli.core.ns.command.MemoryCommand;

public interface CommandApi extends BaseCommand, ControllerCommand, MemoryCommand {

    ConnectionApi getConnectionApi();

}
