package com.dubboagent.utils.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Date:2017/11/26
 *
 * @author:chao.cheng
 **/
public class AgentExtensionLoader<T> {
    private static Logger logger = LoggerFactory.getLogger(AgentExtensionLoader.class);

    private static final String SENDER_DIRECTORY = "META-INF/";


    private volatile Class<?> cachedSettingClass = null;

    private final Class<?> type;


    private static final ConcurrentMap<Class<?>, AgentExtensionLoader<?>> EXTENSION_LOADERS =
            new ConcurrentHashMap();

    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();


    private AgentExtensionLoader(Class<?> type) {
        this.type = type;
    }


    /**
     * 根据type获取扩展加载器
     *
     * @param type
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> AgentExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("AgentExtension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("AgentExtension type(" + type + ") 不是 interface!");
        }

        AgentExtensionLoader<T> loader = (AgentExtensionLoader<T>) EXTENSION_LOADERS.get(type);

        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new AgentExtensionLoader<T>(type));
            loader = (AgentExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }

        EXTENSION_LOADERS.get(type);
        return loader;
    }


    /**
     * 加载Setting Class
     *
     * @param <T>
     * @return
     */
    public <T> T loadSettingClass() {

        if (cachedSettingClass == null) {
            try {
                synchronized (EXTENSION_INSTANCES) {
                    Object cachedSettingObject = EXTENSION_INSTANCES.get(type);
                    if (null == cachedSettingObject) {
                        loadFile(SENDER_DIRECTORY);
                        EXTENSION_INSTANCES.put(type, cachedSettingClass.newInstance());
                    }
                }
            } catch (Throwable e) {
                logger.error("加载扩展类失败! interface:" + type, e);
                return null;
            }
        }
        return (T) EXTENSION_INSTANCES.get(type);
    }


    /**
     * 加载META-INF配置文件
     *
     * @param dir
     */
    private void loadFile(String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }


            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                final int ci = line.indexOf('#');
                                if (ci >= 0) {
                                    line = line.substring(0, ci);
                                }
                                line = line.trim();
                                if (line.length() > 0) {
                                    parseAndInitSettingClass(classLoader, url, line);
                                }
                            }
                        } finally {
                            reader.close();
                        }
                    } catch (Throwable t) {
                        logger.error("AgentException 当加载扩展类时(interface: " +
                                type + ", class file: " + url + ") in " + url, t);
                    }
                } // end of while urls
            }
        } catch (Throwable t) {
            logger.error("AgentException 当加载扩展类时(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }

    }

    /**
     * 解析配置文件并且实例化
     *
     * @param classLoader
     * @param url
     * @param line
     */
    private void parseAndInitSettingClass(ClassLoader classLoader, java.net.URL url, String line) {
        try {
            String name = null;
            int i = line.indexOf('=');
            if (i > 0) {
                name = line.substring(0, i).trim();
                line = line.substring(i + 1).trim();
                logger.info("line======"+line);
            }
            if (line.length() > 0) {
                Class<?> clazz = Class.forName(line, true, classLoader);
                if (!type.isAssignableFrom(clazz)) {
                    throw new IllegalStateException("Error when load extension class(interface: " +
                            type + ", class line: " + clazz.getName() + "), class "
                            + clazz.getName() + "is not subtype of interface.");
                }
                if (clazz.isAnnotationPresent(Setting.class)) {
                    if (cachedSettingClass == null) {
                        cachedSettingClass = clazz;
                    } else if (!cachedSettingClass.equals(clazz)) {
                        throw new IllegalStateException("超过一个Setting class被找到: "
                                + cachedSettingClass.getClass().getName()
                                + ", " + clazz.getClass().getName());
                    }
                } else {
                    logger.error("[agent异常] AgentExtensionLoad加载配置文件异常,必须指定需要使用的类!");
                }
            }
        } catch (Throwable t) {
            IllegalStateException e = new IllegalStateException("加载扩展类失败(interface: " + type + ", class line: "
                    + line + ") in " + url + ", cause: " + t.getMessage(), t);

            throw e;
        }
    }


    /**
     * 获取classLoader配置文件查找器
     *
     * @return
     */
    private static ClassLoader findClassLoader() {
        return AgentExtensionLoader.class.getClassLoader();
    }


    public static void main(String[] args) {
        MessageSender messageSender = AgentExtensionLoader.getExtensionLoader(MessageSender.class)
                .loadSettingClass();
    }


}
