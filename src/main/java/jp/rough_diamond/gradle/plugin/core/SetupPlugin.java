package jp.rough_diamond.gradle.plugin.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupPlugin {
	private final static Logger logger = LoggerFactory.getLogger(SetupPlugin.class);

	public static void setup(Project project) {
		logger.debug("RDF Plugin Setup process starting");
		String classpath = System.getProperty("java.class.path");
		logger.debug("classpath:" + classpath);
		getTargetJarFiles(classpath)
				.map(file -> getTaskRegister(file))
				.forEach(register -> register.register(project));
	}

	static TaskRegister getTaskRegister(File file) {
		try (JarInputStream jis = new JarInputStream(new FileInputStream(file))) {
			JarEntry entry;
			while((entry = jis.getNextJarEntry()) != null) {
				if("META-INF/rdfPlugin.properties".equals(entry.getName())) {
					return getTaskRegister(jis);
				}
			}
			return (p -> {});
		} catch (IOException | InstantiationException |
				IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	static TaskRegister getTaskRegister(InputStream is) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		final Properties prop = new Properties();
		prop.load(is);
		if(!prop.containsKey("register")) {
			return (p -> {});
		}
		return (TaskRegister) Class.forName(prop.getProperty("register")).newInstance();
	}

	static Stream<File> getTargetJarFiles(String param) {
		return Arrays.stream(param.split(File.pathSeparator))
				.map(fileName -> new File(fileName))
				.filter(file -> isJarFile(file));
	}

	private static boolean isJarFile(File file) {
		return file.exists() && !file.isDirectory() && file.getName().endsWith(".jar");
	}
}
