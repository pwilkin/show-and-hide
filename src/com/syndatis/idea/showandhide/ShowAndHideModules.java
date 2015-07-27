package com.syndatis.idea.showandhide;

import com.google.common.collect.Lists;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShowAndHideModules extends DialogWrapper implements TreeCellRenderer, TreeSelectionListener {
    private JTree modulesTree;
    private Project project;
    private JPanel contentPane;

    class NodeData {
        Project project;
        Module module;
        boolean shown;

        NodeData(Project p) {
            shown = true;
            project = p;
        }

        NodeData(Module m, boolean s) {
            shown = s;
            module = m;
        }
    }

    public ShowAndHideModules(Project project) {
        super(project);
        this.project = project;
        init();
        setModal(true);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void createUIComponents() {
        NodeData projectData = new NodeData(project);
        DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(projectData);
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Set<TreePath> selectedPaths = new HashSet<>();
        for (Module module : modules) {
            NodeData modData = new NodeData(module, ModuleShowUtil.isModuleShown(module));
            DefaultMutableTreeNode modNode = new DefaultMutableTreeNode(modData);
            projectNode.add(modNode);
            if (modData.shown) {
                List<DefaultMutableTreeNode> list = Lists.newArrayList(projectNode, modNode);
                TreePath path = new TreePath(list.toArray());
                selectedPaths.add(path);
            }
        }
        modulesTree = new Tree(projectNode);
        modulesTree.setCellRenderer(this);
        modulesTree.addSelectionPaths(selectedPaths.toArray(new TreePath[]{}));
        modulesTree.addTreeSelectionListener(this);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object node, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (node instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) node).getUserObject() instanceof NodeData) {
            NodeData value = (NodeData) ((DefaultMutableTreeNode) node).getUserObject();
            if (value.project != null) {
                JBLabel lab = new JBLabel(value.project.getName());
                lab.setIcon(IconLoader.getIcon("/nodes/project.png"));
                return lab;
            } else if (value.module != null) {
                JBLabel mod = new JBLabel(value.module.getName());
                mod.setIcon(IconLoader.getIcon("/nodes/Module.png"));
                return mod;
            } else {
                return new JBLabel(" *** UNKNOWN *** "); // should not happen
            }
        } else {
            return new JBLabel(" *** UNKNOWN *** "); // should not happen
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Tree tree = (Tree) e.getSource();
    }

    @Override
    protected void doOKAction() {
        DefaultMutableTreeNode[] selectedNodes = ((Tree) modulesTree).getSelectedNodes(DefaultMutableTreeNode.class, defaultMutableTreeNode -> {
            NodeData data = (NodeData) defaultMutableTreeNode.getUserObject();
            return data != null && data.module != null;
        });
        Set<Module> selectedModules = new HashSet<>();
        for (DefaultMutableTreeNode selectedNode : selectedNodes) {
            NodeData data = (NodeData) selectedNode.getUserObject();
            selectedModules.add(data.module);
        }
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (selectedModules.contains(module)) {
                ModuleShowUtil.showModule(module);
            } else {
                ModuleShowUtil.hideModule(module);
            }
        }

        super.doOKAction();
    }

    @Override
    public String getTitle() {
        return "Select modules to show";
    }
}
