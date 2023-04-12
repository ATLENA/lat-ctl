package io.lat.ctl.configurator;

import io.lat.ctl.common.CommandCtl;
import io.lat.ctl.type.ConfiguratorCommandType;
import io.lat.ctl.type.InstallerServerType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ConfiguratorMapper {
	
    public static Configurator getConfigurator(List<String> commandList) throws Exception {
        String command = commandList.get(0);
        String serverType = commandList.get(1);
        
        log.debug("Start [latctl.sh "+command+" "+serverType+"]");

        InstallerServerType installerServerType=null;
        ConfiguratorCommandType configuratorCommandType=null;

        installerServerType = InstallerServerType.getInstallServerType(serverType);
        configuratorCommandType = ConfiguratorCommandType.valueOf(command.toUpperCase().replace('-','_'));

        if(installerServerType==null){
            CommandCtl.printHelpPage();
            System.exit(1);
        }

        switch (installerServerType){
            case APACHE:
            case TOMCAT:
            case COMET:
            case NGINX:
                switch(configuratorCommandType){
                    case LIST_ENGINES:
                    case SWITCH_VERSION:
                    case DOWNLOAD_ENGINE:
                    case LE:
                    case SV:
                    case DE:
                        return new LatEngineConfigurator(configuratorCommandType, installerServerType);
                }
        }



        return null;
    }
}
