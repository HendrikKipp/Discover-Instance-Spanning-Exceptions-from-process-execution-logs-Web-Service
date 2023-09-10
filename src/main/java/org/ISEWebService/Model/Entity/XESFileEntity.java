package org.ISEWebService.Model.Entity;

import jakarta.persistence.*;
import org.deckfour.xes.model.XLog;

import java.util.List;

// Source: https://www.baeldung.com/java-db-storing-files
@Entity
@Table
public class XESFileEntity {

    private String fileName;

    @Lob
    private byte[] xesFile;

    @Id
    @GeneratedValue
    private Long id;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getXesFile() {
        return xesFile;
    }

    public void setXESFile(byte[] xesFile) {
        this.xesFile = xesFile;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
