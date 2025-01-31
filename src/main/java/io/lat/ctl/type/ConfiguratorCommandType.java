package io.lat.ctl.type;

public enum ConfiguratorCommandType {
    LIST_ENGINES("list-engines"),
    SWITCH_VERSION("switch-version"),
    DOWNLOAD_ENGINE("download-engine"),
    LE("le"),
    SV("sv"),
    DE("de");

    private String command;

    ConfiguratorCommandType(String command){
        this.command=command;
    }

    public String getCommand() {
        return command;
    }
}
