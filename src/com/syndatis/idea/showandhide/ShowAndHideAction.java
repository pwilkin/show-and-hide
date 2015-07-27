package com.syndatis.idea.showandhide;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.WindowManager;

import java.awt.*;

/**
 * Created by pwilkin on 30.05.2015.
 */
public class ShowAndHideAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        WindowManager manager = WindowManager.getInstance();
        Window window = manager.suggestParentWindow(anActionEvent.getProject());
        ShowAndHideModules showAndHide = new ShowAndHideModules(anActionEvent.getProject());
        showAndHide.show();
    }
}
