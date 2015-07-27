package com.syndatis.idea.showandhide;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ExcludeFolder;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredFileBean;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by pwilkin on 31.05.2015.
 */
public class ModuleShowUtil {

    public static boolean isModuleShown(Module m) {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
            ModuleRootManager mrm = ModuleRootManager.getInstance(m);
            Set<VirtualFile> excludeRootsSet = Sets.newHashSet(mrm.getExcludeRoots());
            Set<VirtualFile> contentRootsSet = Sets.newHashSet(mrm.getContentRoots());
            return !excludeRootsSet.containsAll(contentRootsSet);
        });
    }

    public static void showModule(Module module) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            ModuleRootManager mrm = ModuleRootManager.getInstance(module);
            Set<VirtualFile> roots = Sets.newHashSet(mrm.getContentRoots());
            ModifiableRootModel modifiable = mrm.getModifiableModel();
            for (ContentEntry contentEntry : modifiable.getContentEntries()) {
                ExcludeFolder[] ef = contentEntry.getExcludeFolders();
                for (ExcludeFolder e : ef) {
                    if (roots.contains(e.getFile())) {
                        contentEntry.removeExcludeFolder(e);
                    }
                }
            }
            modifiable.commit();
            ChangeListManager manager = ChangeListManager.getInstance(module.getProject());
            IgnoredFileBean[] filesToIgnoreArray = manager.getFilesToIgnore();
            if (filesToIgnoreArray != null) {
                List<IgnoredFileBean> filesToIgnore = Lists.newArrayList(filesToIgnoreArray);
                Iterator<IgnoredFileBean> iterator = filesToIgnore.iterator();
                while (iterator.hasNext()) {
                    IgnoredFileBean ignoredFileBean = iterator.next();
                    for (VirtualFile virtualFile : mrm.getContentRoots()) {
                        if (ignoredFileBean.matchesFile(virtualFile)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                manager.setFilesToIgnore(filesToIgnore.toArray(new IgnoredFileBean[] {}));
            }

        });
    }

    public static void hideModule(Module module) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            ModuleRootManager mrm = ModuleRootManager.getInstance(module);
            ModifiableRootModel modifiable = mrm.getModifiableModel();
            for (ContentEntry contentEntry : modifiable.getContentEntries()) {
                VirtualFile root = contentEntry.getFile();
                Set<VirtualFile> exFiles = Sets.newHashSet(contentEntry.getExcludeFolderFiles());
                if (!exFiles.contains(root)) {
                    contentEntry.addExcludeFolder(root);
                }
            }
            modifiable.commit();
        });
    }
}
