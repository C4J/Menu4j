package com.commander4j.tree;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.commander4j.sys.Common;

public class JMenuTreeStateSaver {


    public void saveTreeState() {
    	
    	Path filePath = Paths.get(Common.treeFolderFile+".state");
    	
        if (Common.tree == null || Common.tree.getModel() == null || Common.tree.getModel().getRoot() == null) {
            System.err.println("Tree or tree model is null, cannot save state.");
            return;
        }

        List<String> expandedPaths = new ArrayList<>();
        TreeNode root = (TreeNode) Common.tree.getModel().getRoot();
        if (root == null) return;

        Enumeration<TreePath> expandedDescendants = Common.tree.getExpandedDescendants(new TreePath(root));
        if (expandedDescendants != null) {
            while (expandedDescendants.hasMoreElements()) {
                TreePath path = expandedDescendants.nextElement();
                expandedPaths.add(path.toString());
            }
        }

        if (Common.tree.isExpanded(new TreePath(root)) && !expandedPaths.contains(new TreePath(root).toString()) ) {

             if (Common.tree.isExpanded(0) && !expandedPaths.contains(new TreePath(root).toString())) {
                 expandedPaths.add(new TreePath(root).toString());
             }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (String pathString : expandedPaths) {
                writer.write(pathString);
                writer.newLine();
            }
            System.out.println("Tree state saved to "+filePath);
        } catch (IOException e) {
            System.err.println("Error saving JTree state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadTreeState(JTree tree) {
    	
    	Path filePath = Paths.get(Common.treeFolderFile+".state");
    	 System.out.println("Loading Tree State from "+filePath);
    	 
        if (tree == null || tree.getModel() == null || tree.getModel().getRoot() == null) {
            System.err.println("Tree or tree model is null, cannot load state.");
            return;
        }
        if (!Files.exists(filePath)) {
            System.err.println("State file not found: " + filePath);
            return;
        }

        collapseAllNodes(tree);

        List<String> pathStrings;
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            pathStrings = reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error loading JTree state: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        Object root = model.getRoot();
        if (root == null) return;

        for (String pathStr : pathStrings) {
            // Example pathStr: "[Root, Beverages, Milkshakes]"
            if (pathStr.startsWith("[") && pathStr.endsWith("]")) {
                String content = pathStr.substring(1, pathStr.length() - 1);
                if (content.isEmpty()) continue; // Should not happen for valid paths

                String[] nodeNames = content.split(",\\s*"); // Split by comma and optional whitespace

                List<Object> nodePath = new ArrayList<>();
                Object currentNode = root;

                // Check root first
                if (nodeNames.length > 0 && currentNode.toString().equals(nodeNames[0])) {
                    nodePath.add(currentNode);
                } else {
                    System.err.println("Root mismatch: Expected '" + nodeNames[0] + "', Got '" + currentNode.toString() + "' for path: " + pathStr);
                    continue; // Skip this path if root doesn't match
                }

                // Traverse for subsequent nodes
                for (int i = 1; i < nodeNames.length; i++) {
                    String targetNodeName = nodeNames[i];
                    TreeNode parentNode = (TreeNode) currentNode;
                    Object foundChild = null;
                    for (int j = 0; j < model.getChildCount(parentNode); j++) {
                        TreeNode child = (TreeNode) model.getChild(parentNode, j);
                        if (child.toString().equals(targetNodeName)) {
                            foundChild = child;
                            break;
                        }
                    }
                    if (foundChild != null) {
                        nodePath.add(foundChild);
                        currentNode = foundChild;
                    } else {
                        System.err.println("Could not find node '" + targetNodeName + "' under '" + parentNode.toString() + "' for path: " + pathStr);
                        nodePath.clear(); // Invalidate path
                        break;
                    }
                }

                if (!nodePath.isEmpty()) {
                    TreePath treePathToExpand = new TreePath(nodePath.toArray());
                    tree.expandPath(treePathToExpand);
                }
            } else {
                System.err.println("Malformed path string in state file: " + pathStr);
            }
        }

        tree.revalidate();
        tree.repaint();
    }

    public void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            // If expanding caused new rows to be added, recurse
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    public void collapseAllNodes(JTree tree) {
        if (tree == null || tree.getModel() == null || tree.getModel().getRoot() == null) {
            return;
        }

        int row = tree.getRowCount() - 1;
        while (row >= 0) {
            tree.collapseRow(row);
            row--;
        }

        if (tree.isRootVisible() && tree.getShowsRootHandles() && tree.getRowCount() > 0) {
           // tree.collapseRow(0); // If you want root always collapsed initially
        } else if (tree.getRowCount() > 0){
            tree.expandRow(0); // Typically, you want the root at least visible/expanded.
        }

    }

}