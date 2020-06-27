package com.machiav3lli.backup.handler;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class TarArchive implements Closeable {

    private final TarArchiveOutputStream archive;

    public TarArchive(OutputStream os){
        this.archive = new TarArchiveOutputStream(os);
        this.archive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
    }

    /**
     * Adds a filepath to the archive.
     * If it's a directory, it'll be added cursively
     * @param inputFilepath the filepath to add to the archive
     * @param parent the parent directory in the archive, use "" to add it to the root directory
     * @throws IOException on IO related errors such as out of disk space or missing files
     */
    public void addFilepath(File inputFilepath, String parent) throws IOException {
        String entryName = parent + inputFilepath.getName();

        this.archive.putArchiveEntry(new TarArchiveEntry(inputFilepath, entryName));
        if(inputFilepath.isFile()){
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFilepath));
            IOUtils.copy(bis, this.archive);
            this.archive.closeArchiveEntry();
            bis.close();
        }else if(inputFilepath.isDirectory()){
            for(File nextFile : Objects.requireNonNull(inputFilepath.listFiles(), "Directory listing returned null!")){
                this.addFilepath(nextFile, entryName + File.separator);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.archive.close();
    }
}
