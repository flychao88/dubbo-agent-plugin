package com.snifferagent.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Date:2017/12/6
 *
 * @author:chao.cheng
 **/
public class PropertiesLoadUtils {

    private static Logger logger = LoggerFactory.getLogger(PropertiesLoadUtils.class);


    private static File findPath() throws Exception {
        String classResourcePath = PropertiesLoadUtils.class.getName().replaceAll("\\.", "/") + ".class";

        URL resource = PropertiesLoadUtils.class.getClassLoader().getSystemClassLoader().getResource(classResourcePath);
        if (resource != null) {
            String urlString = resource.toString();

            logger.debug("The beacon class location is {}.", urlString);

            int insidePathIndex = urlString.indexOf('!');
            boolean isInJar = insidePathIndex > -1;

            if (isInJar) {
                urlString = urlString.substring(urlString.indexOf("file:"), insidePathIndex);
                File agentJarFile = null;
                try {
                    agentJarFile = new File(new URL(urlString).getFile());
                } catch (MalformedURLException e) {
                    logger.error("Can not locate agent jar file by url:" + urlString, e);
                }
                if (agentJarFile.exists()) {
                    return agentJarFile.getParentFile();
                }
            } else {
                String classLocation = urlString.substring(urlString.indexOf("file:"), urlString.length() - classResourcePath.length());
                return new File(classLocation);
            }
        }

        logger.error("Can not locate agent jar file.");
        throw new Exception("Can not locate agent jar file.");
    }


    public static void init(String path, Class<?> configClzz) {

        InputStream in = null;
        Properties prop = new Properties();
        try {
            File configFile = new File(findPath(), path);
            in = new FileInputStream(configFile);
            //读取属性文件a.properties
            prop.load(in);

            Field[] fields = configClzz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                String value = prop.getProperty(field.getName().toLowerCase());
                if (value != null) {
                    Class<?> type = field.getType();
                    if (type.equals(int.class)) {
                        field.set(null, Integer.valueOf(value));
                    } else if (type.equals(String.class)) {
                        field.set(null, value);
                    } else if (type.equals(long.class)) {
                        field.set(null, Long.valueOf(value));
                    } else if (type.equals(boolean.class)) {
                        field.set(null, Boolean.valueOf(value));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[read file error] 读取配置文件出错! path:"+path, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.error("[file stream close error]  path:"+path, e);
                }
            }
        }
    }


}
