package com.github.lgdd.liferay.starter.services;

import org.apache.commons.compress.utils.IOUtils;

import javax.inject.Singleton;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Singleton
public class ArchiveService {

  public void compressZipfile(String sourceDir, OutputStream os) throws IOException {
    var zos = new ZipOutputStream(os);
    compressDirectoryToZipfile(sourceDir, sourceDir, zos);
    IOUtils.closeQuietly(zos);
  }

  private void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out)
      throws IOException {
    var fileList = new File(sourceDir).listFiles();

    if (fileList == null) {
      throw new FileNotFoundException();
    }

    if (fileList.length == 0) { // empty directory / empty folder
      ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "") + "/");
      out.putNextEntry(entry);
      out.closeEntry();
    } else {
      for (File file : fileList) {
        if (file.isDirectory()) {
          compressDirectoryToZipfile(rootDir, sourceDir + File.separator + file.getName(), out);
        } else {
          ZipEntry entry = new ZipEntry(
              sourceDir.replace(rootDir, "") + File.separator + file.getName());
          out.putNextEntry(entry);

          FileInputStream in = new FileInputStream(sourceDir + File.separator + file.getName());
          IOUtils.copy(in, out);
          IOUtils.closeQuietly(in);
        }
      }
    }
  }
}
