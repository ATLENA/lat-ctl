package io.lat.ctl.common;

public class ControllerCommandCtl {
    final String START = "START";
    final String STOP = "STOP";

    public ControllerCommandCtl() {}

    public boolean containsCommand(String command) {
        boolean result = false;

        if(START.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }else if(STOP.toLowerCase().equals(command.toLowerCase())){
            result = true;
        }
        return result;
    }
}
