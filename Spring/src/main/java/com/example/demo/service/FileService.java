package com.example.demo.service;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileService {

  public Map<String, Object> getFileTree(String rootPath) {
    File root = new File(rootPath);
    return buildFileTree(root);
  }

  private Map<String, Object> buildFileTree(File dir) {
    Map<String, Object> node = new HashMap<>();
    node.put("name", dir.getName());
    node.put("type", dir.isDirectory() ? "folder" : "file");

    if (dir.isDirectory()) {
      List<Map<String, Object>> children = new ArrayList<>();
      File[] files = dir.listFiles();

      if (files != null) {
        for (File file : files) {

          children.add(buildFileTree(file));
        }
      }
      node.put("children", children);
    }
    return node;
  }
}
