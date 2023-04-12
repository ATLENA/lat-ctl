package io.lat.ctl.configurator;

import io.lat.ctl.exception.LatException;
import io.lat.ctl.type.ConfiguratorCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EngineUtil;
import io.lat.ctl.util.InstallInfoUtil;
import io.lat.ctl.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

@Slf4j
public class LatEngineConfigurator extends LatConfigurator {
    private ConfiguratorCommandType configuratorCommandType;
    private static InstallerServerType installerServerType;


    public LatEngineConfigurator(ConfiguratorCommandType configuratorCommandType, InstallerServerType installerServerType) throws Exception {
        this.configuratorCommandType = configuratorCommandType;
        this.installerServerType = installerServerType;

        switch (configuratorCommandType){
            case LIST_ENGINES:
            case LE:
                EngineUtil.listEngines(getServerType());
                break;
            case DOWNLOAD_ENGINE:
            case DE:
                System.out.println("Available "+getServerType()+" engine versions:");
                EngineUtil.listEngines(getServerType());
                downloadEngine();
                break;
            case SWITCH_VERSION:
            case SV:
                switchEngineVersion();
                break;
        }

    }
    protected static String getServerType() {
        return installerServerType.getServerType();
    }

   private void downloadEngine() throws Exception {


        System.out.println("=====================================================");
        System.out.println("Select ENGN_VERSION to download. Versions with '*' are already downloaded.");
        System.out.println("ex : 9.0.00.A.RELEASE, 2.4.02.A.RELEASE");
        System.out.print(": ");


        Scanner scan = new Scanner(System.in);
        String version = scan.nextLine();
        
        

        EngineUtil.downloadEngine(version, getServerType());
   }

   private void switchEngineVersion() {

        //TODO : stopped 상태인지 검증 로직 추가
        //TODO : SERVER_ID 도 INSTANCE_ID로 바꿔야 하는 것 아닌지?
        //TODO : 설치된 instance list 보여주는 로직 추가
	   try {
        Scanner scan = new Scanner(System.in);
        Collection<File> installedList = EngineUtil.getInstalledEngines(getServerType());

        /*
        System.out.println("Enter INSTANCE_ID to switch version");
        System.out.print(": ");
        String instanceId = scan.nextLine();
*/
        System.out.println("| 1. Select instance to delete. Enter number of the instance or instance ID");
		ArrayList<Element> elementList = InstallInfoUtil.getServerByType(InstallerServerType.getInstallServerType(getServerType()));
		for(int i=0; i<elementList.size(); i++) {
			Element e = elementList.get(i);
			
			System.out.println("| ("+(i+1)+") "+XmlUtil.getValueByTagName(e, "id")+" "+XmlUtil.getValueByTagName(e, "path"));
		}

		
		//System.out.println("| 1. INSTANCE_ID : Instance ID to delete                                                  ");
		System.out.print("|: ");
		String instanceId = scan.nextLine();
		
		if(instanceId.compareTo("1")>=0 && instanceId.compareTo(elementList.size()+"")<=0) {
			instanceId = XmlUtil.getValueByTagName(elementList.get(Integer.parseInt(instanceId)-1),"id");
		}
        
        
        if(!InstallInfoUtil.existsServer(instanceId, getServerType())) {
        	
        	throw new LatException(instanceId+" doesn't exist. Check the INSTANCE_ID again.");
        }

        
        System.out.println("Installed "+getServerType()+" engine versions are:");
        System.out.println("=====================================================");
        for(File f:installedList) {
        	System.out.println(f.getName());
        }
        System.out.println("=====================================================");
        System.out.println("Select ENGN_VERSION to switch to");
        System.out.print(": ");
        String version = scan.nextLine();

        EngineUtil.switchEngineVersion(instanceId, version, getServerType());
	   }catch(Throwable e) {
		   log.error(e.getMessage());
	   }
   }

}
