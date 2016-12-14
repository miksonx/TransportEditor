package com.oskopek.transporteditor.view.executables;

import java.nio.file.Path;
import java.util.Optional;

public interface ExecutableWithParameters {

    String getExecutable();

    Optional<Path> findExecutablePath();

    default boolean isExecutableValid() {
        return findExecutablePath().isPresent();
    }

    String getParameters(Object... params);

    String getParameters();

}