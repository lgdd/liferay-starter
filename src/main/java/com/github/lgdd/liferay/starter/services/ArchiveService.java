package com.github.lgdd.liferay.starter.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.inject.Singleton;
import org.apache.commons.compress.utils.IOUtils;

/**
 * Compress directories and files recursively.
 */
@Singleton
public class ArchiveService {

  public void compressZipFile(String sourceDir, OutputStream os) throws IOException {
    var zos = new ZipOutputStream(os);
    compressDirectoryToZipFile(sourceDir, sourceDir, zos);
    IOUtils.closeQuietly(zos);
  }

  private void compressDirectoryToZipFile(String rootDir, String sourceDir, ZipOutputStream out)
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
          compressDirectoryToZipFile(rootDir, sourceDir + File.separator + file.getName(), out);
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
