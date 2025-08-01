package priv.koishi.tools.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.net.URI;

import static priv.koishi.tools.Finals.CommonFinals.*;
import static priv.koishi.tools.Utils.UiUtils.addToolTip;

/**
 * @author KOISHI
 * Date:2025-08-01
 * Time:18:53
 */
public class PMCAboutController {

    @FXML
    public ImageView logo_PMC;

    @FXML
    public Label title_PMC;

    @FXML
    public Button openGitHubLinkBtn_PMC, openGiteeLinkBtn_PMC, openBaiduLinkBtn_PMC, openQuarkLinkBtn_PMC,
            openXunleiLinkBtn_PMC;

    /**
     * 设置鼠标悬停提示
     */
    private void setToolTip() {
        // 给github、gitee跳转按钮添加鼠标悬停提示
        addToolTip(tip_openGitLink, openGitHubLinkBtn_PMC, openGiteeLinkBtn_PMC);
        // 给网盘跳转按钮添加鼠标悬停提示
        addToolTip(tip_openLink, openBaiduLinkBtn_PMC, openQuarkLinkBtn_PMC, openXunleiLinkBtn_PMC);
    }

    /**
     * 界面初始化
     *
     */
    @FXML
    private void initialize() {
        // 设置鼠标悬停提示
        setToolTip();
    }

    /**
     * 打开百度云盘链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openBaiduLink() throws Exception {
        Desktop.getDesktop().browse(new URI(baiduLinkPMC));
    }

    /**
     * 打开夸克云盘链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openQuarkLink() throws Exception {
        Desktop.getDesktop().browse(new URI(quarkLinkPMC));
    }

    /**
     * 打开迅雷云盘链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openXunleiLink() throws Exception {
        Desktop.getDesktop().browse(new URI(xunleiLinkPMC));
    }

    /**
     * 打开GitHub链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openGitHubLink() throws Exception {
        Desktop.getDesktop().browse(new URI(githubLinkPMC));
    }

    /**
     * 打开Gitee链接
     *
     * @throws Exception 链接打开失败
     */
    @FXML
    private void openGiteeLink() throws Exception {
        Desktop.getDesktop().browse(new URI(giteeLinkPMC));
    }

}
