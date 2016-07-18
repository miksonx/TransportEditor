package com.oskopek.transporteditor.persistence;

import com.oskopek.transporteditor.domain.StudyPlan;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Basic methods for writing a {@link com.oskopek.transporteditor.domain.StudyPlan} to persistent data storage.
 */
public interface DataWriter {

    /**
     * Writes a {@link StudyPlan} to a file in the filesystem.
     *
     * @param plan the StudyPlan to persist
     * @param fileName the file to write to
     * @throws IOException if something writing the plan failed
     * @throws IllegalArgumentException if plan or fileName is null
     */
    void writeTo(StudyPlan plan, String fileName) throws IOException, IllegalArgumentException;

    /**
     * Writes a {@link StudyPlan} to an {@link OutputStream}.
     *
     * @param plan the StudyPlan to persist
     * @param outputStream the stream to write to
     * @throws IOException if something writing the plan failed
     * @throws IllegalArgumentException if plan or outputStream is null
     */
    void writeTo(StudyPlan plan, OutputStream outputStream) throws IOException, IllegalArgumentException;

}
