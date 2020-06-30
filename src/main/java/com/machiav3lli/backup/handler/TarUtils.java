package com.machiav3lli.backup.handler;

import android.system.ErrnoException;
import android.system.Os;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public final class TarUtils {

    /**
     * Adds a filepath to the given archive.
     * If it's a directory, it'll be added cursively
     *
     * @param archive       an opened tar archive to write to
     * @param inputFilepath the filepath to add to the archive
     * @param parent        the parent directory in the archive, use "" to add it to the root directory
     * @throws IOException on IO related errors such as out of disk space or missing files
     */
    public static void addFilepath(TarArchiveOutputStream archive, File inputFilepath, String parent) throws IOException {
        String entryName = parent + inputFilepath.getName();
        TarArchiveEntry archiveEntry = new TarArchiveEntry(inputFilepath, entryName);
        // Interject for symlinks
        if(FileUtils.isSymlink(inputFilepath)){
            archiveEntry.setLinkName(inputFilepath.getCanonicalPath());
        }
        archive.putArchiveEntry(archiveEntry);

        if (inputFilepath.isFile() && !FileUtils.isSymlink(inputFilepath)) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFilepath));
            IOUtils.copy(bis, archive);
            bis.close();
            archive.closeArchiveEntry();
        } else if (inputFilepath.isDirectory()) {
            archive.closeArchiveEntry();
            for (File nextFile : Objects.requireNonNull(inputFilepath.listFiles(), "Directory listing returned null!")) {
                TarUtils.addFilepath(archive, nextFile, entryName + File.separator);
            }
        }else{
            // in case of a symlink
            archive.closeArchiveEntry();
        }
    }

    public static void uncompressTo(TarArchiveInputStream archive, File targetDir) throws IOException {
        TarArchiveEntry tarEntry;
        while ((tarEntry = archive.getNextTarEntry()) != null) {
            final File file = new File(targetDir, tarEntry.getName());
            if (tarEntry.isDirectory()) {
                if (!file.mkdirs()) {
                    throw new IOException("Unable to create folder " + file.getAbsolutePath());
                }
            } else if (tarEntry.isSymbolicLink()) {
                try {
                    Os.symlink(tarEntry.getLinkName(), targetDir + tarEntry.getFile().getPath());
                } catch (ErrnoException e) {
                    e.printStackTrace();
                }
            } else {
                final File parent = file.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IOException("Unable to create folder " + parent.getAbsolutePath());
                }
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    IOUtils.copy(archive, fos);
                }
            }
        }
    }
}
