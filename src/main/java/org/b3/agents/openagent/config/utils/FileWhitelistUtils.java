package org.b3.agents.openagent.config.utils;

import java.util.Set;

import org.b3.agents.openagent.model.RepositoryFile;

public class FileWhitelistUtils {
    private static Set<String> backendSet = Set.of(".java", ".kt", ".py", ".cs", ".cpp", ".c", ".go", ".rb", ".php", ".rs", ".scala",".swift",".ts",".js");
    private static Set<String> frontendSet = Set.of(".js",".jsx",".ts",".tsx",".html",".css",".scss",".vue",".svelte");
    private static Set<String> markupSet = Set.of(".json",".yml",".yaml",".xml",".ini",".toml",".md",".properties");

    public static boolean isCodeFile(String fileName, RepositoryFile file) {
        int dot = fileName.lastIndexOf('.');
        if (dot == -1) return false;
        String ext = fileName.substring(dot).toLowerCase();
        file.setExtension(ext);
        if (isBackendFile(ext,file)){
            file.setFileType("backend");
            return true;
        } else if (isFrontendFile(ext,file)) {
            file.setFileType("frontend");
            return true;
        } else if (isMarkupFile(ext,file)){
            file.setFileType("markup");
            return true;
        }
        return false;
    }
    public static boolean isBackendFile(String ext, RepositoryFile file) {
        
        return backendSet.contains(ext.toLowerCase());
    }
    public static boolean isFrontendFile(String ext, RepositoryFile file) {
        
        return frontendSet.contains(ext.toLowerCase());
    }
    public static boolean isMarkupFile(String ext, RepositoryFile file) {
        
        return markupSet.contains(ext.toLowerCase());
    }
}