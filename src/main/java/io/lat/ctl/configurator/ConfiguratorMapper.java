package io.lat.ctl.configurator;

import io.lat.ctl.common.CommandCtl;
import io.lat.ctl.type.ConfiguratorCommandType;
import io.lat.ctl.type.InstallerServerType;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguratorMapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfiguratorMapper.class);
	
    public static Configurator getConfigurator(List<String> commandList) throws Exception {
        String command = commandList.get(0);
        String serverType = commandList.get(1);
        
        LOGGER.debug("Start [latctl.sh "+command+" "+serverType+"]");

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
                switch(configuratorCommandType){
                    case LIST_ENGINES:
                    case SWITCH_VERSION:
                    case DOWNLOAD_ENGINE:
                        return new LatEngineConfigurator(configuratorCommandType, installerServerType);
                }
        }



        return null;
    }
}
