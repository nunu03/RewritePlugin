package com.coofee.rewrite.scan;

import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import com.coofee.rewrite.Rewriter;
import com.coofee.rewrite.util.StringUtil;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class ScanPermissionMethodCallerRewriter extends Rewriter.Adapter {
    private final File rootFolder;
    private final ScanPermissionMethodCallerExtension mScanPermissionMethodCallerExtension;
    private final ScanPermissionMethodCallerResult mResult = new ScanPermissionMethodCallerResult();

    public ScanPermissionMethodCallerRewriter(File rootFolder, ScanPermissionMethodCallerExtension extension) {
        this.rootFolder = rootFolder;
        this.mScanPermissionMethodCallerExtension = extension;
    }

    @Override
    public ClassNode transform(QualifiedContent input, ClassNode classNode) {
        final String className = classNode.name;
        if (this.mScanPermissionMethodCallerExtension.isExcluded(className)) {
            return classNode;
        }

        final String fullClassName = StringUtil.replace(className, '/', '.');
        final String moduleName = input.getName();

        List<MethodNode> methods = classNode.methods;
        for (MethodNode methodNode : methods) {
            InsnList instructions = methodNode.instructions;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            int lineNo = -1;
            while (iterator.hasNext()) {
                AbstractInsnNode next = iterator.next();

                if (next instanceof LineNumberNode) {
                    lineNo = ((LineNumberNode) next).line;
                }

                if (next instanceof MethodInsnNode) {
                    MethodInsnNode insnNode = (MethodInsnNode) next;
                    String insnFullClassName = StringUtil.replace(insnNode.owner, '/', '.');
                    String key = this.mScanPermissionMethodCallerExtension.key(insnFullClassName, insnNode.name);
                    Set<String> permissions = this.mScanPermissionMethodCallerExtension.getPermissions(key);
                    if (permissions != null) {
//                        System.out.println("[RewritePlugin] scan permission method caller key=" + key + ", fullClassName=" + fullClassName + ", method=" + methodNode.name + ", permissions=" + permissions);
                        mResult.add(moduleName, fullClassName, methodNode.name, lineNo, key, permissions);
                    }
                }
            }
        }

        return classNode;
    }

    @Override
    public void postTransform(TransformInvocation transformInvocation) {
        super.postTransform(transformInvocation);

        File resultFile;
        if (mScanPermissionMethodCallerExtension.outputFile == null || mScanPermissionMethodCallerExtension.outputFile.isEmpty()) {
            resultFile = new File(rootFolder, "scan_permission_method_caller_result.json");
        } else {
            resultFile = new File(mScanPermissionMethodCallerExtension.outputFile);
        }

        try {
            String json = new Gson().toJson(mResult.resultByPermissionMap);
            FileUtils.writeStringToFile(resultFile, json);
            System.out.println("[RewritePlugin] scan permission method caller result success write to file " + resultFile.getAbsolutePath());
        } catch (Throwable e) {
            new Throwable("[RewritePlugin] scan permission method caller result fail write to file " + resultFile.getAbsolutePath(), e).printStackTrace();
        }

        File resultByModuleFile = new File(resultFile.getParentFile(), "scan_permission_method_caller_result_by_module.json");
        try {
            String json = new Gson().toJson(mResult.resultByModuleMap);
            FileUtils.writeStringToFile(resultByModuleFile, json);
            System.out.println("[RewritePlugin] scan permission method caller result success write to file " + resultByModuleFile.getAbsolutePath());
        } catch (Throwable e) {
            new Throwable("[RewritePlugin] scan permission method caller result fail write to file " + resultByModuleFile.getAbsolutePath(), e).printStackTrace();
        }
    }


}
