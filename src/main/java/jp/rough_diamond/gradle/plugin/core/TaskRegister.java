package jp.rough_diamond.gradle.plugin.core;

import org.gradle.api.Project;

@FunctionalInterface
public interface TaskRegister {
	public void register(Project p);
}
