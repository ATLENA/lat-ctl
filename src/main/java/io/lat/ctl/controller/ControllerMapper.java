package io.lat.ctl.controller;

import io.lat.ctl.common.CommandCtl;
import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerMapper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMapper.class);

    public static Controller getController(List<String> commandList) throws Exception {
        String command = commandList.get(0);
        String serverType = commandList.get(1);
        String instanceId = commandList.get(2);
        
        LOGGER.debug("Start [latctl.sh "+command+" "+serverType+" "+instanceId+"]");
        		
        ControllerCommandType controllerCommandType = null;
        InstallerServerType controllerServerType = null;

        controllerCommandType = ControllerCommandType.valueOf(command.toUpperCase());
        controllerServerType = InstallerServerType.valueOf(serverType.toUpperCase());

        if(controllerCommandType==null || controllerServerType==null || instanceId==null){
            CommandCtl.printHelpPage();
            System.exit(1);
        }

        switch (controllerServerType){
            case TOMCAT:
                switch (controllerCommandType){
                    case START:
                        return new LatTomcatStartController(controllerCommandType,controllerServerType,instanceId);
                    case STOP:
                        return new LatTomcatStopController(controllerCommandType, controllerServerType, instanceId);
                }
            case NGINX:
                switch (controllerCommandType){
                    case START:
                        return new LatNginxStartController(controllerCommandType, controllerServerType, instanceId);
                    case STOP:
                        return new LatNginxStopController(controllerCommandType, controllerServerType, instanceId);
                }
            case APACHE:
                switch (controllerCommandType){
                    case START:
                        return new LatApacheStartController(controllerCommandType, controllerServerType, instanceId);
                    case STOP:
                        return new LatApacheStopController(controllerCommandType, controllerServerType, instanceId);
                }

            case COMET:
                switch (controllerCommandType){
                    case START:
                        return new LatCometStartController(controllerCommandType, controllerServerType, instanceId);
                    case STOP:
                        return new LatCometStopController(controllerCommandType, controllerServerType, instanceId);
                }
        }

        return null;
    }
}
