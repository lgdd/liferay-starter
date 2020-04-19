package com.github.lgdd.liferay.starter.utils;

import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class ArchiveUtil {

    public static void compressZipfile(String sourceDir, OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        compressDirectoryToZipfile(sourceDir, sourceDir, zos);
        IOUtils.closeQuietly(zos);
    }

    private static void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out) throws IOException, FileNotFoundException {
        File[] fileList = new File(sourceDir).listFiles();
        if (fileList.length == 0) { // empty directory / empty folder
            ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "") + "/");
            out.putNextEntry(entry);
            out.closeEntry();
        } else {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    compressDirectoryToZipfile(rootDir, sourceDir + File.separator + file.getName(), out);
                } else {
                    ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "") + File.separator + file.getName());
                    out.putNextEntry(entry);

                    FileInputStream in = new FileInputStream(sourceDir + File.separator + file.getName());
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                }
            }
        }
    }
}
