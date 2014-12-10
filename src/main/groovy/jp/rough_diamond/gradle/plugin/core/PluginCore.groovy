package jp.rough_diamond.gradle.plugin.core

import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginCore implements Plugin<Project> {
	@Override
	public void apply(Project target) {
		SetupPlugin.setup(target);
	}
}
