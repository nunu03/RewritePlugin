package com.coofee.rewrite.util;

import com.android.build.api.transform.TransformInput;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransformInputUtil {

    public static List<TransformInput> listOf(Collection<TransformInput>... transformInputs) {
        if (transformInputs == null || transformInputs.length < 1) {
            return null;
        }

        List<TransformInput> lists = new ArrayList<>();
        for (Collection<TransformInput> t : transformInputs) {
            lists.addAll(t);
        }


        return lists;
    }

    public static List<File> files(List<TransformInput> transformInputList) {
        if (transformInputList == null || transformInputList.isEmpty()) {
            return null;
        }

        List<File> fileList = new ArrayList<>();
        transformInputList.forEach(transformInput -> {
            transformInput.getDirectoryInputs().forEach(directoryInput -> {
                fileList.add(directoryInput.getFile());
            });

            transformInput.getJarInputs().forEach(jarInput -> {
                fileList.add(jarInput.getFile());
            });
        });

        return fileList;
    }
}
