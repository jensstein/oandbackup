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

    public void addFilepath(String inputFilepath, String parent) throws IOException {
        File file = new File(inputFilepath);
        String entryName = parent + file.getName();

        this.archive.putArchiveEntry(new TarArchiveEntry(file, entryName));
        if(file.isFile()){
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(bis, this.archive);
            this.archive.closeArchiveEntry();
            bis.close();
        }else if(file.isDirectory()){
            for(File nextFile : Objects.requireNonNull(file.listFiles(), "Directory listing returned null!")){
                this.addFilepath(nextFile.getAbsolutePath(), entryName + File.separator);
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.archive.close();
    }
}
