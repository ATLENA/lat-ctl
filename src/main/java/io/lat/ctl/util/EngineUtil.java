package io.lat.ctl.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lat.ctl.exception.LatException;
import io.lat.ctl.installer.LatInstaller;
import io.lat.ctl.type.InstallerServerType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class EngineUtil {

    public static void switchEngineVersion(String serverId, String version, String serverType){



        String installRootPath = FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", serverType);
        String envPath = FileUtil.getConcatPath(installRootPath, serverId, "env.sh");
        String currentVersion = FileUtil.getShellVariableString(envPath, "ENGN_VERSION");
        currentVersion = currentVersion.substring(13);
        
        if(version.startsWith(serverType+"-")) {
        	version = version.substring(serverType.length()+1);
        }


        /*
        Process proc;

        //TODO refactoring
        List<String> args = new ArrayList<String>();
        System.out.println(installRootPath+"/"+server_id+"/stop.sh");
        args.add(installRootPath+"/"+server_id+"/stop.sh");
        try {
            proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
        }
        catch (IOException e) {
            System.out.println("Failed to stop the instance.");
            return;
        }


         */
        String targetPath = InstallInfoUtil.getServerInstallPath(serverId, InstallerServerType.getInstallServerType(serverType));

        if (StringUtil.isBlank(targetPath)) {
            throw new LatException(serverId + " doesn't exist.");
        }

        if (isRunning(targetPath, "ps")) {
            throw new LatException(serverId + " is running.");
        }


        System.out.println("VERSION: "+version);
        System.out.println("CURRENT VERSION: "+currentVersion);
        
        if(version.equals(currentVersion)) {
        	log.error(version+" is current version.");
        	return;
        	//throw new LatException(version+" is current version.");
        }

        String[] splitVersion = version.split("\\.");
        String[] splitCurrentVersion = currentVersion.split("\\.");

        for(int i=0; i<2; i++){
            if(!splitVersion[i].equals(splitCurrentVersion[i])){
                System.out.println("It is not allowed to modify the major version");
                return;
            }
        }

        Collection<File> runtimes = getInstalledEngines(serverType);

        Iterator<File> it = runtimes.iterator();
        boolean isInstalled = false;
        
        while(it.hasNext()){
        	String name = it.next().getName();

            if(name.substring(serverType.length()+1).equals(version)) {
                isInstalled=true;
                break;
            }
        }
        if(isInstalled) {
            FileUtil.setShellVariable(envPath, "ENGN_VERSION", version);
            return;
        }

        System.out.println(version+" is not installed. Install the version first.");
        return;
    }

    public static Collection<File> getInstalledEngines(String serverType){
        String runtimePath = FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", serverType);

        Collection<File> runtimes = CustomFileUtils.listDirectories(new File(runtimePath), new WildcardFileFilter("*"), TrueFileFilter.INSTANCE);

        return runtimes;
    }


    public static void listEngines(String serverType) throws IOException {
        List<String> availableList = getEnginesFromGithub(serverType);
        Collection<File> installedList = getInstalledEngines(serverType);
        String latestVersion = LatInstaller.getEngineVersion(serverType);

        for(String version : availableList){

            version = version.substring(0, version.length()-7);


            if(compareVersion(latestVersion, version.substring(serverType.length()+1))>0){
                continue;
            }

            System.out.print(version);

            Iterator<File> it = installedList.iterator();
            while(it.hasNext()){
                File file = it.next();
                if(file.getName().equals(version)){
                    System.out.print(" *");
                    it.remove();
                    break;
                }
            }
            System.out.println();
        }

        Iterator<File> left = installedList.iterator();
        while(left.hasNext()){
            System.out.println(left.next().getName()+" *");
        }
    }
    
    
    
    public static List<String> getEnginesFromGithub(String serverType) throws IOException {
        String URL = "https://api.github.com/repos/ATLENA/lat-"+serverType+"-runtimes/git/trees/main";

        //TODO STATUS CODE 로 에러 처리
        HttpClient client = HttpClients.createDefault();
        HttpGet getMethod = new HttpGet(URL);
        HttpResponse httpResponse = client.execute(getMethod);
        //int statusCode = httpResponse.getStatusLine().getStatusCode();
        //String response = httpResponse.getEntity().toString();
        
        
        
        
        
        
        //HttpClient client = new HttpClient();
        //GetMethod getMethod = new GetMethod(URL);
        //int statusCode = client.executeMethod(getMethod);
        //String response = getMethod.getResponseBodyAsString();

        Gson gson = new Gson();
        //JsonObject jo = gson.fromJson(response, JsonObject.class);
        JsonObject jo = gson.fromJson(EntityUtils.toString(httpResponse.getEntity()), JsonObject.class);

        List<String> re = new ArrayList<String>();

        JsonArray ja = jo.get("tree").getAsJsonArray();

        for(int i=0; i<ja.size(); i++){
            String file = ja.get(i).getAsJsonObject().get("path").getAsString();
            if(file.endsWith(".tar.gz")){
                re.add(file);
            }

        }
        
        

        return re;
    }

    public static void downloadEngine(String version, String serverType) throws Exception {

    	if(version.startsWith(serverType+"-")) {
    		version = version.substring(serverType.length()+1);
    	}
    	
    	String FILE_NAME = serverType+"-"+version+".tar.gz";
        String FILE_URL = "https://github.com/ATLENA/lat-"+serverType+"-runtimes/raw/main/"+FILE_NAME;
        String FILE_PATH = FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", serverType);


        System.out.println("Downloading file from "+FILE_URL+".....");


        ReadableByteChannel rbc = Channels.newChannel(new URL(FILE_URL).openStream());
        FileOutputStream fos = new FileOutputStream(FILE_NAME);

        fos.getChannel().transferFrom(rbc, 0,  Long.MAX_VALUE);
        fos.close();

        System.out.println("Downloading completed.");

        File compressedFile = new File(FILE_NAME);
        File decompressedFile = new File(FILE_NAME.substring(0,FILE_NAME.length()-3));
        decompressedFile = decompress(compressedFile, decompressedFile);

        //System.out.println("tar = "+decompressedFile.getName());
        System.out.println("Path to install = "+FILE_PATH);

        File[] files = unarchive(decompressedFile, new File(FileUtil.getConcatPath(FILE_PATH, serverType+"-"+version)));
        
        for(File file:files) {
        	FileUtil.chmod755(file);
        }
        
        CustomFileUtils.deleteQuietly(compressedFile);
        CustomFileUtils.deleteQuietly(decompressedFile);
    }

    public static File decompress(File compressedFile, File decompressedFile) {
        InputStream inputStream = null;
        CompressorInputStream compressorInputStream = null;
        FileOutputStream outputStream = null;

        try {
            inputStream = new FileInputStream(compressedFile);
            compressorInputStream = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, inputStream);
          
            
            outputStream = new FileOutputStream(decompressedFile);
            IOUtils.copy(compressorInputStream, outputStream);

        } catch (CompressorException e) {
            throw new IllegalStateException("Fail to compress a file.", e);
        } catch (IOException e) {
            throw new LatException("An I/O error has occurred : " + e);
        } finally {
            FileUtil.close(compressorInputStream);
            FileUtil.close(inputStream);
            FileUtil.close(outputStream);
        }
        return decompressedFile;
    }

    /**
     *
     * unarchive를 수행한다.<br>
     *
     * @param compressedFile archive 파일명
     * @param destinationDirectory unarchive 대상이 되는 디렉토리 경로
     * @return unarchive된 파일 경로 리스트
     *
     */
    public static File[] unarchive(File compressedFile, File destinationDirectory) {
        File[] resultList = null;

        try {
            final InputStream inputStream = new FileInputStream(compressedFile);
            ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();


            ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(ArchiveStreamFactory.TAR, inputStream);

            byte[] buffer = new byte[65536];

            TarArchiveEntry entry = (TarArchiveEntry)archiveInputStream.getNextEntry();

            while (entry != null) {
                String name = entry.getName();

                name = name.replace('\\', '/');

                File destinationFile = new File(destinationDirectory, name);
                // extract 버그 수정
                if (!name.endsWith("/")) {
                    File parentFolder = destinationFile.getParentFile();
                    if (!parentFolder.exists()) {
                        parentFolder.mkdirs();
                    }

                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(destinationFile);
                        int length = archiveInputStream.read(buffer);
                        while (length != -1) {
                            if (length > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                            length = archiveInputStream.read(buffer);
                        }
                    } finally {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                }
                else{
                    destinationFile.mkdirs();
                }
                entry = (TarArchiveEntry)archiveInputStream.getNextEntry();
            }
            resultList = destinationDirectory.listFiles();
            FileUtil.close(inputStream);

        } catch (ArchiveException e) {
            throw new IllegalStateException("Fail to unarchive the files.", e);
        } catch (IOException e) {
            throw new LatException("An I/O error has occurred : " + e);
        }
        return resultList;
    }

    public static int compareVersion(String version1, String version2)
    {
        String[] arr1 = version1.split("\\.");
        String[] arr2 = version2.split("\\.");

        // same number of version "." dots
        for (int i = 0; i < 3; i++)
        {
            if(Integer.parseInt(arr1[i]) < Integer.parseInt(arr2[i]))
                return -1;
            if(Integer.parseInt(arr1[i]) > Integer.parseInt(arr2[i]))
                return 1;
        }

        if(arr1[3].equals(arr2[3])) return 0;
        else return arr1[3].compareTo(arr2[3]);
        // went through all version numbers and they are all the same
    }

    public static boolean isRunning(String targetPath, String commandFileName) {
        boolean res = true;

        String[] cmd = new String[]{FileUtil.getConcatPath(targetPath) + "/" + commandFileName + ".sh"};

        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = br.readLine();

            if(s == null) {
                res = false;
            }
        } catch (Exception e) {
            throw new LatException(e);
        }

        return res;
    }
}
