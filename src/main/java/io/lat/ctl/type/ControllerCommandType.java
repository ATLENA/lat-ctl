package io.lat.ctl.type;

public enum ControllerCommandType {


    START("start"),
    STOP("stop"),
    RESTART("restart");

    private String command;

    ControllerCommandType(String command){
        this.command=command;
    }

    public String getCommand() {
        return command;
    }
}
