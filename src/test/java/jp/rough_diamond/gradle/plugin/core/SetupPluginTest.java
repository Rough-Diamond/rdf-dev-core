package jp.rough_diamond.gradle.plugin.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SetupPluginTest {
	@Test
	public void クラスパスからjarファイルのみを切り出せる事() {
		String param = String.join(File.pathSeparator,
				"fake.jar", "includeTargetSetupInfo.jar", "withoutTargetSetupInfo.jar",
				"src/test/java/jp/rough_diamond/gradle/plugin/core/SetupPluginTest.java");
		System.out.println(param);
		File[] files = SetupPlugin.getTargetJarFiles(param).toArray(count -> new File[count]);
		assertThat("返却数が誤っています。", files.length, is(2));
		assertThat("ファイル名が誤っています。", files[0].getName(), is("includeTargetSetupInfo.jar"));
		assertThat("ファイル名が誤っています。", files[1].getName(), is("withoutTargetSetupInfo.jar"));
	}

	@Test
	public void Jarファイルのメタ情報からタスク登録実装クラスの取得が出来る事() {
		TaskRegister register = SetupPlugin.getTaskRegister(new File("includeTargetSetupInfo.jar"));
		assertThat("正しく取得できませんでした。",
				(Object)register.getClass(),  is((Object)SampleRegister.class));
	}

//	@Test
//	public void Jarファイルのメタ情報からタスク情報を読み込める事() {
//		Map<String, ? extends Closure<?>> map = SetupPlugin.getTasks(new File("includeTargetSetupInfo.jar"));
//		assertThat("タスク情報が読み込めていません。", map.size(), is(1));
//	}

	@Before
	public void setup() {
		makeJars();
	}

	@After
	public void teardown() {
		removeJars();
	}

	private void makeJars() {
		action((Consumer<? super File>) f -> makeJar(f));
	}

	private BiConsumer<File, String> consumer;
	private void makeJar(File f) {
		System.out.println(f.getName());
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(getJarName(f)))){
			Arrays.stream(f.listFiles()).forEach(file-> {
				consumer = (File current, String root) -> {
					try {
						if(current.isDirectory()) {
							Arrays.stream(current.listFiles()).forEach(
									child -> consumer.accept(child, root + current.getName() + "/"));
						} else {
							System.out.println(root + current.getName());
							JarEntry entry = new JarEntry(root + current.getName());
							jos.putNextEntry(entry);
							jos.write(FileUtils.readFileToByteArray(current));
							jos.closeEntry();
						}
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				};
				consumer.accept(file, "");
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void removeJars() {
		action((Consumer<? super File>) f -> removeJar(f));
	}

	private void removeJar(File f) {
		File file = new File(getJarName(f));
		file.delete();
	}

	private void action(Consumer<? super File> action) {
		File root = new File("src/test/resources");
		Arrays.stream(root.listFiles()).filter(f -> f.isDirectory()).forEach(action);
	}

	private String getJarName(File f) {
		return f.getName() + ".jar";
	}
}
