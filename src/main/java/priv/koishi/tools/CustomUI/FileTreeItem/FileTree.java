package priv.koishi.tools.CustomUI.FileTreeItem;

import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TreeItem;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import lombok.Getter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.IntBuffer;
import java.util.function.Function;

/**
 * 文件树
 *
 * @author KOISHI
 * Date:2025-08-04
 * Time:19:02
 */
public class FileTree extends TreeItem<String> {

    /**
     * 系统根目录文件对象，通过文件系统视图获取的第一个根目录作为树的顶层节点
     */
    public static File ROOT_FILE = FileSystemView.getFileSystemView().getRoots()[0];

    /**
     * 子节点初始化状态标记（true表示尚未加载子节点，false表示已完成初始化）
     */
    private boolean notInitialized = true;

    /**
     * 当前节点关联的文件对象
     */
    @Getter
    private final File file;

    /**
     * 子文件列表提供器，用于自定义子节点加载策略的函数式接口
     */
    private final Function<File, File[]> supplier;

    /**
     * 带文件参数的构造函数，使用默认子节点加载策略初始化树节点
     *
     * @param file 关联的文件对象
     */
    public FileTree(File file) {
        super(getFileName(file), getFileIconToNode(file));
        this.file = file;
        supplier = (File f) -> {
            if (((FileTree) this.getParent()).getFile() == ROOT_FILE) {
                String name = getFileName(f);
                // 过滤无法展开的目录
                if (name.equals("网络") || name.equals("家庭组")) {
                    return new File[0];
                }
            }
            return f.listFiles();
        };
    }

    /**
     * 完全自定义构造函数
     *
     * @param file     关联的文件对象
     * @param supplier 子文件列表提供器函数
     */
    public FileTree(File file, Function<File, File[]> supplier) {
        super(getFileName(file), getFileIconToNode(file));
        this.file = file;
        this.supplier = supplier;
    }

    /**
     * 重写子节点获取方法，实现延迟加载机制，在节点首次展开时加载子节点
     *
     * @return 子节点的可观察列表
     */
    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        ObservableList<TreeItem<String>> children = super.getChildren();
        //没有加载子目录时，则加载子目录作为树节点的孩子
        if (this.notInitialized && this.isExpanded()) {
            //设置没有初始化为假
            this.notInitialized = false;
            // 判断树节点的文件是否是目录， 如果是目录，着把目录里面的所有的文件目录添加入树节点的孩子中。
            if (this.getFile().isDirectory()) {
                for (File f : supplier.apply(this.getFile())) {
                    //如果文件是目录，则把它加到树节点上
                    if (f.isDirectory()) {
                        children.add(new FileTree(f));
                    }
                }
            }
        }
        return children;
    }

    /**
     * 重写叶子节点判断方法，根据文件是否为目录判断是否为叶子节点
     *
     * @return true 当前节点对应非目录文件
     */
    @Override
    public boolean isLeaf() {
        return !file.isDirectory();
    }

    /**
     * 文件图标转换方法，将系统图标转换为JavaFX Canvas对象用于节点显示
     *
     * @param file 需要获取图标的文件对象
     * @return 包含文件图标的Canvas对象
     */
    public static Canvas getFileIconToNode(File file) {
        //获取系统文件的图标
        Image image = ((ImageIcon) FileSystemView.getFileSystemView()
                .getSystemIcon(file)).getImage();
        //构建图片缓冲区，设定图片缓冲区的大小和背景，背景为透明
        BufferedImage bi = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.BITMASK);
        //把图片画到图片缓冲区
        bi.getGraphics().drawImage(image, 0, 0, null);
        //将图片缓冲区的数据转换成int型数组
        int[] data = ((DataBufferInt) bi.getData().getDataBuffer()).getData();
        //获得写像素的格式模版
        WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbInstance();
        //新建javafx的画布
        Canvas canvas = new Canvas(bi.getWidth() + 2, bi.getHeight() + 2);
        //获取像素的写入器
        PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
        //根据写像素的格式模版把int型数组写到画布
        pixelWriter.setPixels(1, 1, bi.getWidth(), bi.getHeight(),
                pixelFormat, data, 0, bi.getWidth());
        //设置树节点的图标
        return canvas;
    }

    /**
     * 获取文件显示名称，使用系统文件视图获取文件显示名称
     *
     * @param file 需要获取名称的文件对象
     * @return 系统显示的文件名称
     */
    public static String getFileName(File file) {
        return FileSystemView.getFileSystemView().getSystemDisplayName(file);
    }

}
