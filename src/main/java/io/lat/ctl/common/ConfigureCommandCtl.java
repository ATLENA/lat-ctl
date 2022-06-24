package io.lat.ctl.common;

import org.apache.commons.cli.Options;

public class ConfigureCommandCtl {
    final String LIST_ENGINES = "LIST-ENGINES";
    final String DOWNLOAD_ENGINE = "DOWNLOAD-ENGINE";
    final String SWITCH_VERSION = "SWITCH-VERSION";

    Options options = null;

    public ConfigureCommandCtl() {
        initCommandOptions();
    }
    public void initCommandOptions() {

    }

    public boolean containsCommand(String command){
        boolean result=false;
        if(LIST_ENGINES.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }
        else if(DOWNLOAD_ENGINE.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }
        else if(SWITCH_VERSION.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }

        return result;
    }
}
