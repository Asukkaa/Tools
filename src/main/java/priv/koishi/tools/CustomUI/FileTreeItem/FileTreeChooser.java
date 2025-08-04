package priv.koishi.tools.CustomUI.FileTreeItem;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static priv.koishi.tools.CustomUI.FileTreeItem.FileTree.ROOT_FILE;

/**
 * 文件树选择器
 *
 * @author KOISHI
 * Date:2025-08-04
 * Time:19:55
 */
public class FileTreeChooser extends Stage {

    /**
     * 文件选择器确认选中监听器
     */
    @Setter
    private SelectionListener selectionListener;

    /**
     * 文件树选择器树视图
     */
    private final TreeView<String> treeView;

    /**
     * 文件树选择器确定按钮
     */
    private final Button okButton;

    /**
     * 创建文件树选择器
     *
     * @param owner 父窗口
     */
    public FileTreeChooser(Window owner) {
        // 初始化UI组件
        VBox vBox = new VBox(10);
        treeView = buildStringTreeView();
        HBox hBox = new HBox(10);
        // 创建带参数的按钮
        okButton = new Button("确定");
        Button cancelButton = new Button("取消");
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(okButton, cancelButton);
        // 布局设置
        vBox.getChildren().addAll(treeView, hBox);
        StackPane stackPane = new StackPane(vBox);
        Scene scene = new Scene(stackPane);
        // 窗口配置
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setMinWidth(200);
        setMinHeight(500);
        setScene(scene);
        // 绑定取消按钮事件
        cancelButton.setOnAction(e -> close());
        okButton.setOnAction(e -> {
            List<File> selectedFiles = getSelectedFiles();
            if (selectionListener != null) {
                try {
                    selectionListener.onFilesSelected(selectedFiles);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            close();
        });
        show();
    }

    /**
     * 设置确定按钮的文字
     *
     * @param okText 确定按钮的文字
     */
    public void setOkText(String okText) {
        okButton.setText(okText);
    }

    /**
     * 获取选中的文件
     *
     * @return 选中的文件
     */
    public List<File> getSelectedFiles() {
        return treeView.getSelectionModel().getSelectedItems().stream()
                .map(item -> ((FileTree) item).getFile())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 创建文件树
     *
     * @return 文件树
     */
    private TreeView<String> buildStringTreeView() {
        TreeView<String> treeView = new TreeView<>();
        FileTree fileTree = new FileTree(ROOT_FILE, f -> {
            File[] allFiles = f.listFiles();
            File[] directorFiles = f.listFiles(File::isDirectory);
            List<File> list = null;
            if (allFiles != null) {
                list = new ArrayList<>(Arrays.asList(allFiles));
                if (directorFiles != null) {
                    list.removeAll(Arrays.asList(directorFiles));
                }
            }
            if (list != null) {
                return list.toArray(new File[0]);
            }
            return null;
        });
        treeView.setRoot(fileTree);
        treeView.setShowRoot(false);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return treeView;
    }

}
