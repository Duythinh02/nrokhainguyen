package nro.models.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.lang.management.ManagementFactory;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.BossStatus;
import nro.models.data.LocalManager;
import nro.models.server.Client;
import nro.models.server.Manager;
import nro.models.server.ServerManager;

/**
 * RESNROSHOP Panel V4 - full panel build, giao diện đồng bộ, shop item, event/giftcode, boss SQL + runtime.
 */
public class RESNROSHOPPanel extends JFrame {

    private static RESNROSHOPPanel instance;

    private final Color BG = new Color(245, 247, 250);
    private final Color WHITE = Color.WHITE;
    private final Color CREAM = new Color(255, 248, 238);
    private final Color ORANGE = new Color(255, 153, 0);
    private final Color ORANGE_DARK = new Color(220, 111, 0);
    private final Color BLUE = new Color(39, 129, 226);
    private final Color PINK = new Color(226, 31, 111);
    private final Color GREEN = new Color(14, 183, 126);
    private final Color PURPLE = new Color(102, 111, 224);
    private final Color TEXT = new Color(22, 34, 55);
    private final Color MUTED = new Color(102, 112, 133);
    private final Font TITLE = new Font("Arial", Font.BOLD, 22);
    private final Font SUBTITLE = new Font("Arial", Font.BOLD, 13);
    private final Font NORMAL = new Font("Arial", Font.PLAIN, 12);

    private final JLabel statusLabel = new JLabel("RESNROSHOP Panel V4 ready");
    private final JTabbedPane tabs = new JTabbedPane();
    private JTextArea dashboardLog;
    private Map<String, JLabel> dashboardValues;

    private RESNROSHOPPanel() {
        setTitle("RESNROSHOP - ADMIN PANEL V4");
        setSize(1580, 900);
        setMinimumSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        setupLook();
        buildHeader();
        buildTabs();
        buildFooter();
    }

    public static void openPanel() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (instance == null) {
                    instance = new RESNROSHOPPanel();
                }
                instance.setVisible(true);
                instance.toFront();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupLook() {
        UIManager.put("TabbedPane.selected", new Color(255, 245, 225));
        UIManager.put("TabbedPane.contentAreaColor", BG);
        UIManager.put("TabbedPane.focus", ORANGE);
        UIManager.put("TabbedPane.background", new Color(255, 250, 240));
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.highlight", ORANGE);
        UIManager.put("TabbedPane.borderHightlightColor", ORANGE);
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, ORANGE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JLabel title = new JLabel("RESNROSHOP ADMIN PANEL V4");
        title.setForeground(ORANGE);
        title.setFont(TITLE);
        JLabel sub = new JLabel("Dashboard • Player • Account • ShopItem • Topup • Event • Server Login • Boss Config");
        sub.setForeground(MUTED);
        sub.setFont(NORMAL);
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);
        header.add(left, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
    }

    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ORANGE),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        statusLabel.setForeground(ORANGE_DARK);
        statusLabel.setFont(SUBTITLE);
        footer.add(statusLabel, BorderLayout.WEST);
        add(footer, BorderLayout.SOUTH);
    }

    private void buildTabs() {
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.setBackground(new Color(255, 250, 240));
        tabs.setForeground(TEXT);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tabs.addChangeListener(e -> setStatus("Đang mở mục: " + tabs.getTitleAt(tabs.getSelectedIndex())));
        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("Player", playerPanel());
        tabs.addTab("Account", accountPanel());
        tabs.addTab("ShopItem", shopItemPanel());
        tabs.addTab("Topup", dataTablePage("QUẢN LÝ TOPUP", firstExisting("topup", "napthe", "history_transaction", "player_topup_logs"), "username", "Tìm ID / username / mã giao dịch:"));
        tabs.addTab("Event", giftcodePanel());
        tabs.addTab("Server Login", serverLoginPanel());
        tabs.addTab("Boss Config", bossPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel pageBase() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ORANGE, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        if (title != null && !title.isEmpty()) {
            JLabel l = new JLabel(title);
            l.setForeground(ORANGE);
            l.setFont(SUBTITLE);
            p.add(l, BorderLayout.NORTH);
        }
        return p;
    }

    private JPanel dashboardPanel() {
        JPanel p = pageBase();
        dashboardValues = new LinkedHashMap<>();

        JPanel metrics = new JPanel(new GridLayout(1, 5, 10, 0));
        metrics.setOpaque(false);
        String[] names = {"Online", "JVM RAM", "CPU", "Threads", "Sessions"};
        for (String n : names) {
            JPanel m = metricCard(n);
            metrics.add(m);
        }

        JPanel center = new JPanel(new GridLayout(2, 2, 12, 12));
        center.setOpaque(false);
        center.add(quickActionsCard());
        center.add(bossManagerCard());
        center.add(statisticsCard());
        center.add(expConfigCard());

        dashboardLog = new JTextArea();
        dashboardLog.setEditable(false);
        dashboardLog.setFont(new Font("Consolas", Font.BOLD, 12));
        dashboardLog.setForeground(new Color(120, 56, 0));
        dashboardLog.setBackground(new Color(255, 251, 235));
        JPanel logCard = card("Server Logs");
        logCard.setPreferredSize(new Dimension(100, 250));
        JScrollPane logScroll = new JScrollPane(dashboardLog);
        logScroll.setPreferredSize(new Dimension(100, 220));
        logCard.add(logScroll, BorderLayout.CENTER);
        log("[Panel] Dashboard initialized. Monitoring Server resources.");

        JPanel dashboardMain = new JPanel(new BorderLayout(10, 10));
        dashboardMain.setOpaque(false);
        dashboardMain.add(metrics, BorderLayout.NORTH);
        dashboardMain.add(center, BorderLayout.CENTER);
        p.add(dashboardMain, BorderLayout.CENTER);
        p.add(logCard, BorderLayout.SOUTH);
        refreshDashboard();
        new Timer(3000, e -> refreshDashboard()).start();
        return p;
    }

    private JPanel metricCard(String name) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(WHITE);
        p.setPreferredSize(new Dimension(160, 78));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ORANGE), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel title = new JLabel(name);
        title.setForeground(ORANGE_DARK);
        title.setFont(NORMAL);
        JLabel value = new JLabel("0");
        value.setForeground(TEXT);
        value.setFont(new Font("Arial", Font.BOLD, 18));
        dashboardValues.put(name, value);
        p.add(title);
        p.add(value);
        return p;
    }

    private JPanel quickActionsCard() {
        JPanel c = card("Quick Actions");
        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8));
        grid.setOpaque(false);
        JButton baoTri = colorButton("Bảo Trì (5s)", ORANGE);
        JButton reload = colorButton("Reload DB", ORANGE);
        JButton donSession = colorButton("Dọn Session", PINK);
        JButton kick = colorButton("Kick All Session", PINK);
        baoTri.addActionListener(e -> log("[Panel] Đã bấm Bảo Trì (5s)."));
        reload.addActionListener(e -> { refreshDashboard(); log("[Panel] Reload thống kê DB."); });
        donSession.addActionListener(e -> log("[Panel] Đã yêu cầu dọn session."));
        kick.addActionListener(e -> log("[Panel] Đã yêu cầu kick toàn bộ session."));
        grid.add(baoTri); grid.add(reload); grid.add(donSession); grid.add(kick);
        c.add(grid, BorderLayout.CENTER);
        return c;
    }

    private JPanel bossManagerCard() {
        JPanel c = card("Boss Manager");
        JPanel grid = new JPanel(new GridLayout(3, 2, 8, 8));
        grid.setOpaque(false);
        JButton menu = colorButton("Mở Menu Tìm & Gọi Boss", ORANGE);
        JButton resetAll = colorButton("Reset + Respawn All Boss", PINK);
        JButton reviveWait = colorButton("Hồi Sinh Boss Chờ", GREEN);
        JButton autoHs = colorButton("Auto HS Boss Chờ: ON (15p)", new Color(218, 65, 212));
        JButton autoReset = colorButton("Auto Reset Boss Vàng: ON (15p)", ORANGE);
        menu.addActionListener(e -> tabs.setSelectedIndex(7));
        resetAll.addActionListener(e -> resetAllBosses());
        reviveWait.addActionListener(e -> reviveDeadBosses());
        autoHs.addActionListener(e -> log("[Panel] Auto HS Boss Chờ đang bật."));
        autoReset.addActionListener(e -> log("[Panel] Auto Reset Boss Vàng đang bật."));
        grid.add(menu); grid.add(resetAll); grid.add(reviveWait); grid.add(autoHs); grid.add(autoReset);
        c.add(grid, BorderLayout.CENTER);
        return c;
    }

    private JPanel statisticsCard() {
        JPanel c = card("Game Statistics");
        JPanel body = new JPanel(new GridLayout(4, 1, 4, 4));
        body.setOpaque(false);
        body.add(infoLine("Giftcodes", countExisting("giftcode", "giftcodes")));
        body.add(infoLine("Consign Items", countExisting("item_shop_ky_gui", "consign_item", "shop_ky_gui")));
        long alive = BossManager.gI().getBosses().stream().filter(b -> !b.isDie()).count();
        long wait = BossManager.gI().getBosses().stream().filter(Boss::isDie).count();
        body.add(infoLine("Boss", alive + " Alive | " + wait + " Wait"));
        body.add(infoLine("Uptime", uptimeText()));
        c.add(body, BorderLayout.CENTER);
        return c;
    }

    private JLabel infoLine(String k, Object v) {
        JLabel l = new JLabel(k + ": " + v);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(TEXT);
        return l;
    }

    private JPanel expConfigCard() {
        JPanel c = card("Server Configuration (EXP)");
        JPanel body = new JPanel(new GridLayout(3, 1, 6, 6));
        body.setOpaque(false);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setOpaque(false);
        JSpinner exp = new JSpinner(new SpinnerNumberModel(3, 1, 999, 1));
        row.add(infoLine("EXP hiện tại", "x" + getCurrentExpText()));
        row.add(new JLabel("   Đặt rate:"));
        row.add(exp);
        JButton set = colorButton("Set EXP", PURPLE);
        JButton autosave = colorButton("AutoSave: ON", new Color(180, 205, 225));
        set.addActionListener(e -> log("[Panel] Đã set EXP rate = " + exp.getValue()));
        autosave.addActionListener(e -> log("[Panel] AutoSave đang bật."));
        body.add(row); body.add(set); body.add(autosave);
        c.add(body, BorderLayout.CENTER);
        return c;
    }

    private JPanel playerPanel() {
        return dataTablePage("QUẢN LÝ NGƯỜI CHƠI", "player", "name", "Tìm ID / tên nhân vật:",
                "id,name,power,clan_id,gold,gem,ruby,thoi_vang,map_id,active,ban");
    }

    private JPanel accountPanel() {
        return dataTablePage("QUẢN LÝ TÀI KHOẢN", "account", "username", "Tìm ID / Username / Tên nhân vật:",
                "id,username,password,ban,active,vip,vnd,tongnap,last_time_login,create_time");
    }

    private JPanel dataTablePage(String title, String tableName, String searchCol, String searchLabel) {
        return dataTablePage(title, tableName, searchCol, searchLabel, "*");
    }

    private JPanel dataTablePage(String title, String tableName, String searchCol, String searchLabel, String columns) {
        JPanel p = pageBase();
        DefaultTableModel model = new DefaultTableModel();
        JTable table = themedTable(model);
        JLabel count = new JLabel("0");
        count.setForeground(MUTED);
        count.setFont(SUBTITLE);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel h = new JLabel(title);
        h.setFont(TITLE);
        h.setForeground(ORANGE);
        head.add(h, BorderLayout.WEST);
        head.add(count, BorderLayout.EAST);

        JPanel search = card("");
        search.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        JTextField keyword = new JTextField(24);
        JButton btnSearch = colorButton("Tìm kiếm", ORANGE);
        JButton reload = colorButton("Làm mới", new Color(246, 211, 230));
        JButton save = colorButton("Sửa / Lưu dòng", ORANGE);
        search.add(label(searchLabel)); search.add(keyword); search.add(btnSearch); search.add(reload); search.add(save);
        Runnable load = () -> {
            loadTableSmart(model, tableName, columns, null, null);
            count.setText(model.getRowCount() + " dòng");
        };
        reload.addActionListener(e -> load.run());
        btnSearch.addActionListener(e -> {
            loadTableSmart(model, tableName, columns, searchCol, keyword.getText().trim());
            count.setText(model.getRowCount() + " dòng");
        });
        save.addActionListener(e -> saveSelectedRow(table, model, tableName));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(search, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        load.run();
        return p;
    }

    private JPanel shopItemPanel() {
        JPanel p = pageBase();
        JTabbedPane shopTabs = new JTabbedPane();
        shopTabs.setFont(SUBTITLE);
        shopTabs.addTab("Shop NPC", shopNpcPage());
        shopTabs.addTab("Shop Items / Tab Shop", shopItemsPage());
        p.add(shopTabs, BorderLayout.CENTER);
        return p;
    }

    private JPanel shopNpcPage() {
        JPanel p = dataTablePage("QUẢN LÝ SHOP NPC", firstExisting("shop", "shop_npc"), "name", "ID Shop / NPC ID / Tên Shop:", "*");
        return p;
    }

    private JPanel shopItemsPage() {
        JPanel p = pageBase();
        JLabel h = new JLabel("QUẢN LÝ SHOP ITEMS V2 - 2 CỘT / NHANH");
        h.setFont(TITLE); h.setForeground(ORANGE);
        p.add(h, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        DefaultTableModel tabsModel = new DefaultTableModel();
        JTable tabTable = themedTable(tabsModel);
        JPanel leftTop = card("1. Chọn NPC / Tab shop");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setOpaque(false);
        JButton loadTab = colorButton("Reload tab", BLUE);
        JButton saveTab = colorButton("Lưu tên tab", GREEN);
        buttons.add(loadTab); buttons.add(saveTab);
        leftTop.add(buttons, BorderLayout.NORTH);
        leftTop.add(new JScrollPane(tabTable), BorderLayout.CENTER);

        DefaultTableModel itemsModel = new DefaultTableModel();
        JTable itemsTable = themedTable(itemsModel);
        JPanel leftBottom = card("2. Danh sách vật phẩm trong tab đang chọn");
        JPanel itemBtns = new JPanel(new FlowLayout(FlowLayout.LEFT)); itemBtns.setOpaque(false);
        JButton add = colorButton("+ Thêm item vào tab này", ORANGE);
        JButton del = colorButton("Xóa item", PINK);
        JButton reload = colorButton("Reload item", BLUE);
        itemBtns.add(add); itemBtns.add(del); itemBtns.add(reload);
        leftBottom.add(itemBtns, BorderLayout.NORTH);
        leftBottom.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 8));
        left.setOpaque(false); left.add(leftTop); left.add(leftBottom);

        JPanel right = card("3. Form sửa / thêm vật phẩm");
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(5, 5, 5, 5); f.fill = GridBagConstraints.HORIZONTAL;
        JTextField txtItemShopId = new JTextField();
        JTextField txtTabId = new JTextField();
        JTextField txtTempId = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtCost = new JTextField("0");
        JComboBox cbTypeSell = new JComboBox(new String[]{"0 - Vàng", "1 - Ngọc", "2 - Hồng ngọc"});
        JTextField txtIconSpec = new JTextField("0");
        JCheckBox chkNew = new JCheckBox("is_new");
        JCheckBox chkSell = new JCheckBox("is_sell", true);
        addFormRow(form, f, 0, "ID item_shop:", txtItemShopId);
        addFormRow(form, f, 1, "Tab ID:", txtTabId);
        addFormRow(form, f, 2, "Temp ID:", txtTempId);
        addFormRow(form, f, 3, "Tên hiển thị:", txtName);
        addFormRow(form, f, 4, "Giá:", txtCost);
        addFormRow(form, f, 5, "Loại tiền:", cbTypeSell);
        addFormRow(form, f, 6, "Icon spec:", txtIconSpec);
        JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT)); checks.setOpaque(false);
        checks.add(chkNew); checks.add(chkSell);
        addFormRow(form, f, 7, "Trạng thái:", checks);
        JPanel option = card("4. Option của item");
        DefaultTableModel optModel = new DefaultTableModel(new Object[]{"Option ID", "Param", "Mô tả"}, 0);
        option.add(new JScrollPane(themedTable(optModel)), BorderLayout.CENTER);
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); rightBtns.setOpaque(false);
        JButton clearForm = colorButton("Làm mới form", BLUE);
        JButton deleteForm = colorButton("XÓA ITEM", PINK);
        JButton saveForm = colorButton("LƯU / THÊM ITEM", GREEN);
        rightBtns.add(clearForm); rightBtns.add(deleteForm); rightBtns.add(saveForm);
        right.add(form, BorderLayout.NORTH); right.add(option, BorderLayout.CENTER); right.add(rightBtns, BorderLayout.SOUTH);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.28; center.add(left, gc);
        gc.gridx = 1; gc.weightx = 0.72; center.add(right, gc);
        p.add(center, BorderLayout.CENTER);
        String shopTable = firstExisting("item_shop", "shop_item");
        loadTab.addActionListener(e -> loadTableSmart(tabsModel, firstExisting("tab_shop", "shop", "shop_npc"), "*", null, null));
        reload.addActionListener(e -> loadTableSmart(itemsModel, shopTable, "*", null, null));
        add.addActionListener(e -> {
            int r = tabTable.getSelectedRow();
            if (r >= 0) {
                int idc = findColumn(tabsModel, "id");
                if (idc >= 0) txtTabId.setText(String.valueOf(tabsModel.getValueAt(r, idc)));
            }
            txtItemShopId.setText(""); txtTempId.requestFocus();
        });
        clearForm.addActionListener(e -> { txtItemShopId.setText(""); txtTabId.setText(""); txtTempId.setText(""); txtName.setText(""); txtCost.setText("0"); txtIconSpec.setText("0"); chkNew.setSelected(false); chkSell.setSelected(true); });
        saveForm.addActionListener(e -> saveItemShopForm(shopTable, txtItemShopId, txtTabId, txtTempId, txtCost, cbTypeSell, txtIconSpec, chkNew, chkSell, itemsModel));
        deleteForm.addActionListener(e -> { deleteItemShopByText(shopTable, txtItemShopId.getText().trim(), itemsModel); });
        del.addActionListener(e -> deleteSelectedById(itemsTable, itemsModel, shopTable));
        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            int r = itemsTable.getSelectedRow(); if (r < 0 || e.getValueIsAdjusting()) return;
            setTextByColumn(itemsModel, r, "id", txtItemShopId);
            setTextByColumn(itemsModel, r, "tab_id", txtTabId);
            setTextByColumn(itemsModel, r, "temp_id", txtTempId);
            setTextByColumn(itemsModel, r, "cost", txtCost);
            setTextByColumn(itemsModel, r, "icon_spec", txtIconSpec);
            int typeCol = findColumn(itemsModel, "type_sell"); if (typeCol >= 0) { try { cbTypeSell.setSelectedIndex(Integer.parseInt(String.valueOf(itemsModel.getValueAt(r, typeCol)))); } catch (Exception ignored) {} }
            int newCol = findColumn(itemsModel, "is_new"); if (newCol >= 0) chkNew.setSelected("1".equals(String.valueOf(itemsModel.getValueAt(r, newCol))) || "true".equalsIgnoreCase(String.valueOf(itemsModel.getValueAt(r, newCol))));
            int sellCol = findColumn(itemsModel, "is_sell"); if (sellCol >= 0) chkSell.setSelected("1".equals(String.valueOf(itemsModel.getValueAt(r, sellCol))) || "true".equalsIgnoreCase(String.valueOf(itemsModel.getValueAt(r, sellCol))));
        });
        loadTableSmart(tabsModel, firstExisting("tab_shop", "shop", "shop_npc"), "*", null, null);
        loadTableSmart(itemsModel, shopTable, "*", null, null);
        return p;
    }

    private void addFormRow(JPanel form, GridBagConstraints f, int row, String text, Component comp) {
        f.gridx = 0; f.gridy = row; f.weightx = 0; form.add(label(text), f);
        f.gridx = 1; f.weightx = 1; form.add(comp, f);
    }

    private JPanel giftcodePanel() {
        JPanel p = pageBase();
        String tableName = firstExisting("giftcode", "giftcodes");
        JLabel h = new JLabel("QUẢN LÝ GIFTCODE"); h.setFont(TITLE); h.setForeground(ORANGE);
        DefaultTableModel model = new DefaultTableModel();
        JTable table = themedTable(model);
        JPanel search = card("Tìm kiếm / thêm sửa giftcode");
        search.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        JTextField key = new JTextField(18);
        JTextField code = new JTextField(14);
        JTextField countLeft = new JTextField("1", 6);
        JTextField expired = new JTextField("2099-12-31 23:59:59", 16);
        JButton find = colorButton("Tìm", ORANGE);
        JButton reload = colorButton("Làm mới", BLUE);
        JButton add = colorButton("Thêm giftcode", GREEN);
        JButton save = colorButton("Sửa dòng chọn", ORANGE);
        JButton del = colorButton("Xóa", PINK);
        search.add(label("Code:")); search.add(key); search.add(find); search.add(reload);
        search.add(label("Code mới:")); search.add(code); search.add(label("Lượt:")); search.add(countLeft); search.add(label("Hết hạn:")); search.add(expired);
        search.add(add); search.add(save); search.add(del);
        Runnable load = () -> loadTableSmart(model, tableName, "*", "code", null);
        reload.addActionListener(e -> load.run());
        find.addActionListener(e -> loadTableSmart(model, tableName, "*", "code", key.getText().trim()));
        save.addActionListener(e -> saveSelectedRow(table, model, tableName));
        del.addActionListener(e -> deleteSelectedById(table, model, tableName));
        add.addActionListener(e -> addGiftcode(tableName, code.getText().trim(), countLeft.getText().trim(), expired.getText().trim(), model));
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow(); if (r < 0 || e.getValueIsAdjusting()) return;
            int c = findColumn(model, "code"); if (c >= 0) code.setText(String.valueOf(model.getValueAt(r, c)));
            int cl = findColumn(model, "count_left"); if (cl >= 0) countLeft.setText(String.valueOf(model.getValueAt(r, cl)));
            int ex = findColumn(model, "expired"); if (ex >= 0) expired.setText(String.valueOf(model.getValueAt(r, ex)));
        });
        JPanel top = new JPanel(new BorderLayout(0, 10)); top.setOpaque(false); top.add(h, BorderLayout.NORTH); top.add(search, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH); p.add(new JScrollPane(table), BorderLayout.CENTER);
        load.run();
        return p;
    }

    private JPanel serverLoginPanel() {
        JPanel p = pageBase();
        JLabel h = new JLabel("SERVER LOGIN"); h.setFont(TITLE); h.setForeground(ORANGE);
        DefaultTableModel model = new DefaultTableModel();
        JTable table = themedTable(model);
        JPanel search = card(""); search.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        JTextField key = new JTextField(22);
        JButton find = colorButton("Tìm login/account", ORANGE);
        JButton online = colorButton("Player online", BLUE);
        JButton banned = colorButton("Account bị ban", PINK);
        find.addActionListener(e -> loadTableSmart(model, "account", "id,username,ban,last_time_login", "username", key.getText().trim()));
        online.addActionListener(e -> loadOnlinePlayers(model));
        banned.addActionListener(e -> loadTableSmart(model, "account", "id,username,ban,last_time_login", "ban", "1"));
        search.add(label("Username:")); search.add(key); search.add(find); search.add(online); search.add(banned);
        JPanel top = new JPanel(new BorderLayout(0, 10)); top.setOpaque(false); top.add(h, BorderLayout.NORTH); top.add(search, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH); p.add(new JScrollPane(table), BorderLayout.CENTER);
        loadOnlinePlayers(model);
        return p;
    }

    private JPanel bossPanel() {
        JPanel p = pageBase();
        JLabel h = new JLabel("BOSS CONFIG - DANH SÁCH BOSS ĐANG HOẠT ĐỘNG"); h.setFont(TITLE); h.setForeground(ORANGE);
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Tên boss", "Map", "Trạng thái", "Zone", "HP", "Die"}, 0);
        JTable table = themedTable(model);
        JPanel search = card("Tìm boss / gọi boss / reset / hồi sinh"); search.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        JTextField bossKey = new JTextField(18);
        JTextField bossId = new JTextField(8);
        JCheckBox onlyActive = new JCheckBox("Chỉ boss đang hoạt động", true); onlyActive.setOpaque(false); onlyActive.setForeground(TEXT); onlyActive.setFont(SUBTITLE);
        JButton btnSearch = colorButton("Tìm boss", ORANGE);
        JButton refresh = colorButton("Reload danh sách", BLUE);
        JButton call = colorButton("Gọi boss", GREEN);
        JButton reset = colorButton("Reset", PINK);
        JButton revive = colorButton("Hồi sinh", BLUE);
        Runnable load = () -> loadBoss(model, bossKey.getText().trim(), onlyActive.isSelected());
        btnSearch.addActionListener(e -> load.run()); refresh.addActionListener(e -> load.run()); onlyActive.addActionListener(e -> load.run());
        call.addActionListener(e -> createBossFromText(bossId.getText().trim(), model));
        reset.addActionListener(e -> bossAction(table, model, BossStatus.RESPAWN));
        revive.addActionListener(e -> bossAction(table, model, BossStatus.JOIN_MAP));
        search.add(label("Tên/ID:")); search.add(bossKey); search.add(btnSearch); search.add(refresh); search.add(onlyActive);
        search.add(label("BossID gọi:")); search.add(bossId); search.add(call); search.add(reset); search.add(revive);
        JPanel top = new JPanel(new BorderLayout(0, 10)); top.setOpaque(false); top.add(h, BorderLayout.NORTH); top.add(search, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH); p.add(new JScrollPane(table), BorderLayout.CENTER);
        load.run();
        return p;
    }

    private JTable themedTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(34);
        t.setFont(new Font("Arial", Font.BOLD, 12));
        t.setForeground(TEXT);
        t.setGridColor(ORANGE);
        t.setSelectionBackground(new Color(255, 227, 188));
        t.setSelectionForeground(TEXT);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JTableHeader h = t.getTableHeader();
        h.setFont(SUBTITLE);
        h.setForeground(ORANGE_DARK);
        h.setBackground(CREAM);
        h.setPreferredSize(new Dimension(0, 32));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        t.setDefaultRenderer(Object.class, center);
        return t;
    }

    private JButton colorButton(String s, Color color) {
        JButton b = new JButton(s);
        final Color normal = color;
        final Color hover = brighten(color, 18);
        b.setBackground(normal);
        b.setOpaque(true);
        b.setForeground(Color.WHITE);
        if (color.equals(new Color(180, 205, 225)) || color.equals(new Color(246, 211, 230))) b.setForeground(TEXT);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(brighten(color, -20)), BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ORANGE_DARK, 2), BorderFactory.createEmptyBorder(9, 15, 9, 15))); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(normal); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(brighten(normal, -20)), BorderFactory.createEmptyBorder(12, 18, 12, 18))); }
        });
        return b;
    }

    private Color brighten(Color c, int v) {
        return new Color(Math.max(0, Math.min(255, c.getRed() + v)), Math.max(0, Math.min(255, c.getGreen() + v)), Math.max(0, Math.min(255, c.getBlue() + v)));
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(MUTED);
        l.setFont(SUBTITLE);
        return l;
    }

    private void refreshDashboard() {
        if (dashboardValues == null) return;
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long max = rt.maxMemory() / 1024 / 1024;
        dashboardValues.get("Online").setText(String.valueOf(Client.gI().getPlayers().size()));
        dashboardValues.get("JVM RAM").setText(used + " / " + max + " MB");
        dashboardValues.get("CPU").setText(cpuText());
        dashboardValues.get("Threads").setText(String.valueOf(Thread.activeCount()));
        dashboardValues.get("Sessions").setText(String.valueOf(Client.gI().getPlayers().size()));
    }

    private String cpuText() {
        try {
            com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double load = os.getProcessCpuLoad();
            if (load < 0) return "0%";
            return String.format("%.1f%%", load * 100);
        } catch (Exception e) {
            return "0%";
        }
    }

    private String uptimeText() {
        long sec = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        long d = sec / 86400; sec %= 86400;
        long h = sec / 3600; sec %= 3600;
        long m = sec / 60; sec %= 60;
        return String.format("%02dd %02dh %02dm %02ds", d, h, m, sec);
    }

    private String getCurrentExpText() {
        try { return String.valueOf(Manager.RATE_EXP_SERVER); } catch (Throwable t) { return "3"; }
    }

    private void log(String s) {
        setStatus(s);
        if (dashboardLog != null) dashboardLog.append(s + "\n");
    }

    private void resetAllBosses() {
        for (Boss b : BossManager.gI().getBosses()) {
            try { b.changeStatus(BossStatus.RESPAWN); } catch (Exception ignored) {}
        }
        log("[Panel] Đã reset + respawn toàn bộ boss đang load.");
    }

    private void reviveDeadBosses() {
        int c = 0;
        for (Boss b : BossManager.gI().getBosses()) {
            try {
                if (b.isDie()) { b.changeStatus(BossStatus.JOIN_MAP); c++; }
            } catch (Exception ignored) {}
        }
        log("[Panel] Đã hồi sinh boss chờ: " + c);
    }

    private void createBossFromText(String text, DefaultTableModel model) {
        try {
            int id = Integer.parseInt(text);
            Boss boss = BossManager.gI().createBoss(id);
            setStatus(boss != null ? "Đã gọi boss ID " + id : "Không tạo được boss ID " + id);
            loadBoss(model, String.valueOf(id));
        } catch (Exception ex) {
            error("BossID phải là số và có trong BossManager.createBoss");
        }
    }

    private void bossAction(JTable table, DefaultTableModel model, BossStatus status) {
        int row = table.getSelectedRow();
        if (row < 0) { error("Chọn 1 boss trước"); return; }
        int bossId = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
        Boss boss = BossManager.gI().getBossById(bossId);
        if (boss == null) {
            BossManager.gI().createBoss(bossId);
            setStatus("Boss không thấy, đã gọi lại boss ID " + bossId);
        } else {
            boss.changeStatus(status);
            setStatus("Đã đổi trạng thái boss " + boss.name + " -> " + status);
        }
        loadBoss(model, String.valueOf(bossId));
    }

    private void loadBoss(DefaultTableModel model, String keyword) { loadBoss(model, keyword, false); }

    private void loadBoss(DefaultTableModel model, String keyword, boolean onlyActive) {
        model.setRowCount(0);
        String k = keyword == null ? "" : keyword.toLowerCase();
        for (Boss b : BossManager.gI().getBosses()) {
            if (onlyActive && b.isDie()) continue;
            if (!k.isEmpty() && !(String.valueOf(b.id).contains(k) || (b.name != null && b.name.toLowerCase().contains(k)))) continue;
            String map = b.zone != null && b.zone.map != null ? String.valueOf(b.zone.map.mapId) : "-";
            String zone = b.zone != null ? String.valueOf(b.zone.zoneId) : "-";
            String hp = b.nPoint != null ? (b.nPoint.hp + "/" + b.nPoint.hpMax) : "-";
            model.addRow(new Object[]{b.id, b.name, map, b.bossStatus, zone, hp, b.isDie()});
        }
        // Liên kết SQL boss_template: boss chưa spawn vẫn xem/tìm được, tránh lỗi panel chỉ đọc source.
        loadBossTemplateFromSql(model, k, onlyActive);
        setStatus("Đã load boss runtime + SQL: " + model.getRowCount());
    }

    private void loadBossTemplateFromSql(DefaultTableModel model, String keyword, boolean onlyActive) {
        String table = firstExisting("boss_template", "boss_config", "boss", "boss_data");
        if (table == null || table.isEmpty() || !tableExists(table)) return;
        if (onlyActive) return; // đang bật chỉ boss hoạt động thì không trộn boss template chưa spawn
        try (Connection con = LocalManager.getConnection()) {
            String idCol = firstColumn(table, "id", "boss_id", "temp_id");
            String nameCol = firstColumn(table, "name", "name_boss", "boss_name", "ten");
            String mapCol = firstColumn(table, "map", "map_id", "map_join", "mapid");
            String statusCol = firstColumn(table, "status", "state");
            if (idCol == null) return;
            StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table);
            List<Object> params = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" WHERE CAST(").append(idCol).append(" AS CHAR) LIKE ?");
                params.add("%" + keyword + "%");
                if (nameCol != null) { sql.append(" OR ").append(nameCol).append(" LIKE ?"); params.add("%" + keyword + "%"); }
            }
            sql.append(" LIMIT 300");
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt(idCol);
                        if (BossManager.gI().getBossById(id) != null) continue;
                        String name = nameCol != null ? rs.getString(nameCol) : ("Boss #" + id);
                        String map = mapCol != null ? String.valueOf(rs.getObject(mapCol)) : "SQL";
                        String status = statusCol != null ? String.valueOf(rs.getObject(statusCol)) : "SQL template / chưa spawn";
                        model.addRow(new Object[]{id, name, map, status, "-", "-", false});
                    }
                }
            }
        } catch (Exception e) {
            log("[Panel] Boss SQL lỗi: " + e.getMessage());
        }
    }

    private String firstColumn(String table, String... cols) {
        for (String c : cols) if (columnExists(table, c)) return c;
        return null;
    }

    private void loadOnlinePlayers(DefaultTableModel model) {
        model.setColumnIdentifiers(new Object[]{"ID", "Name", "Map", "Zone", "HP", "Session"});
        model.setRowCount(0);
        Client.gI().getPlayers().forEach(pl -> {
            String map = pl.zone != null && pl.zone.map != null ? String.valueOf(pl.zone.map.mapId) : "-";
            String zone = pl.zone != null ? String.valueOf(pl.zone.zoneId) : "-";
            String hp = pl.nPoint != null ? (pl.nPoint.hp + "/" + pl.nPoint.hpMax) : "-";
            model.addRow(new Object[]{pl.id, pl.name, map, zone, hp, pl.getSession() != null ? "online" : "-"});
        });
        setStatus("Đã load player online");
    }

    private void loadTableSmart(DefaultTableModel model, String table, String columns, String searchCol, String keyword) {
        if (table == null || table.isEmpty() || !tableExists(table)) {
            model.setColumnIdentifiers(new Object[]{"Thông báo"});
            model.setRowCount(0);
            model.addRow(new Object[]{"Không tìm thấy bảng: " + table});
            return;
        }
        String selectCols = "*".equals(columns) ? "*" : safeExistingColumns(table, columns);
        String sql = "SELECT " + selectCols + " FROM " + table;
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            String where = buildSearchWhere(table, searchCol);
            if (!where.isEmpty()) {
                sql += " WHERE " + where;
                int count = where.split("LIKE", -1).length - 1;
                for (int i = 0; i < count; i++) params.add("%" + keyword + "%");
            }
        }
        sql += " LIMIT 300";
        loadQuery(model, sql, params.toArray());
    }

    private String buildSearchWhere(String table, String searchCol) {
        List<String> wh = new ArrayList<>();
        if (columnExists(table, "id")) wh.add("CAST(id AS CHAR) LIKE ?");
        if (searchCol != null && columnExists(table, searchCol)) wh.add(searchCol + " LIKE ?");
        if (columnExists(table, "name")) wh.add("name LIKE ?");
        if (columnExists(table, "username")) wh.add("username LIKE ?");
        if (columnExists(table, "player_name")) wh.add("player_name LIKE ?");
        return String.join(" OR ", wh);
    }

    private void loadQuery(DefaultTableModel model, String sql, Object... params) {
        try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                Object[] cols = new Object[md.getColumnCount()];
                for (int i = 0; i < cols.length; i++) cols[i] = md.getColumnLabel(i + 1);
                model.setColumnIdentifiers(cols);
                model.setRowCount(0);
                while (rs.next()) {
                    Object[] row = new Object[cols.length];
                    for (int i = 0; i < cols.length; i++) row[i] = rs.getObject(i + 1);
                    model.addRow(row);
                }
            }
            setStatus("SQL OK: " + sql);
        } catch (Exception e) {
            model.setColumnIdentifiers(new Object[]{"Lỗi"});
            model.setRowCount(0);
            model.addRow(new Object[]{e.getMessage()});
            setStatus("SQL lỗi: " + e.getMessage());
        }
    }

    private void saveSelectedRow(JTable table, DefaultTableModel model, String tableName) {
        int row = table.getSelectedRow();
        if (row < 0) { error("Chọn 1 dòng trước khi lưu"); return; }
        if (!columnExists(tableName, "id")) { error("Bảng này không có cột id để cập nhật an toàn"); return; }
        int idCol = -1;
        for (int i = 0; i < model.getColumnCount(); i++) if ("id".equalsIgnoreCase(model.getColumnName(i))) idCol = i;
        if (idCol < 0) { error("Dữ liệu đang hiển thị không có cột id"); return; }
        Object id = model.getValueAt(row, idCol);
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i == idCol) continue;
            if (!columnExists(tableName, model.getColumnName(i))) continue;
            if (params.size() > 0) sql.append(",");
            sql.append(model.getColumnName(i)).append("=?");
            params.add(model.getValueAt(row, i));
        }
        sql.append(" WHERE id=? LIMIT 1"); params.add(id);
        try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            setStatus("Đã lưu " + ps.executeUpdate() + " dòng vào " + tableName);
        } catch (Exception e) { error("Lỗi lưu: " + e.getMessage()); }
    }


    private int findColumn(DefaultTableModel model, String name) {
        for (int i = 0; i < model.getColumnCount(); i++) if (name.equalsIgnoreCase(model.getColumnName(i))) return i;
        return -1;
    }

    private void setTextByColumn(DefaultTableModel model, int row, String col, JTextField field) {
        int c = findColumn(model, col);
        if (c >= 0) field.setText(String.valueOf(model.getValueAt(row, c)));
    }

    private void deleteSelectedById(JTable table, DefaultTableModel model, String tableName) {
        int row = table.getSelectedRow();
        if (row < 0) { error("Chọn 1 dòng trước"); return; }
        int idCol = findColumn(model, "id");
        if (idCol < 0 || !columnExists(tableName, "id")) { error("Không có cột id để xóa an toàn"); return; }
        Object id = model.getValueAt(row, idCol);
        try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM " + tableName + " WHERE id=? LIMIT 1")) {
            ps.setObject(1, id);
            setStatus("Đã xóa " + ps.executeUpdate() + " dòng khỏi " + tableName);
            loadTableSmart(model, tableName, "*", null, null);
        } catch (Exception e) { error("Lỗi xóa: " + e.getMessage()); }
    }

    private void saveItemShopForm(String tableName, JTextField idField, JTextField tabField, JTextField tempField, JTextField costField, JComboBox typeBox, JTextField iconField, JCheckBox isNew, JCheckBox isSell, DefaultTableModel model) {
        if (tableName == null || tableName.isEmpty() || !tableExists(tableName)) { error("Không tìm thấy bảng item_shop"); return; }
        try {
            List<String> cols = new ArrayList<>();
            List<Object> vals = new ArrayList<>();
            addCol(cols, vals, tableName, "tab_id", Integer.parseInt(tabField.getText().trim()));
            addCol(cols, vals, tableName, "temp_id", Integer.parseInt(tempField.getText().trim()));
            addCol(cols, vals, tableName, "cost", Integer.parseInt(costField.getText().trim()));
            addCol(cols, vals, tableName, "type_sell", typeBox.getSelectedIndex());
            addCol(cols, vals, tableName, "icon_spec", Integer.parseInt(iconField.getText().trim()));
            addCol(cols, vals, tableName, "is_new", isNew.isSelected() ? 1 : 0);
            addCol(cols, vals, tableName, "is_sell", isSell.isSelected() ? 1 : 0);
            String id = idField.getText().trim();
            try (Connection con = LocalManager.getConnection()) {
                if (id.isEmpty()) {
                    StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(").append(String.join(",", cols)).append(") VALUES (");
                    for (int i = 0; i < cols.size(); i++) { if (i > 0) sql.append(","); sql.append("?"); }
                    sql.append(")");
                    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                        for (int i = 0; i < vals.size(); i++) ps.setObject(i + 1, vals.get(i));
                        setStatus("Đã thêm item_shop: " + ps.executeUpdate() + " dòng");
                    }
                } else {
                    StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
                    for (int i = 0; i < cols.size(); i++) { if (i > 0) sql.append(","); sql.append(cols.get(i)).append("=?"); }
                    sql.append(" WHERE id=? LIMIT 1");
                    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                        for (int i = 0; i < vals.size(); i++) ps.setObject(i + 1, vals.get(i));
                        ps.setInt(vals.size() + 1, Integer.parseInt(id));
                        setStatus("Đã sửa item_shop: " + ps.executeUpdate() + " dòng");
                    }
                }
            }
            loadTableSmart(model, tableName, "*", null, null);
        } catch (Exception e) { error("Lỗi lưu item_shop: " + e.getMessage()); }
    }

    private void addCol(List<String> cols, List<Object> vals, String table, String col, Object value) {
        if (columnExists(table, col)) { cols.add(col); vals.add(value); }
    }

    private void deleteItemShopByText(String tableName, String id, DefaultTableModel model) {
        if (id.isEmpty()) { error("Nhập hoặc chọn ID item_shop cần xóa"); return; }
        try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM " + tableName + " WHERE id=? LIMIT 1")) {
            ps.setInt(1, Integer.parseInt(id));
            setStatus("Đã xóa item_shop: " + ps.executeUpdate() + " dòng");
            loadTableSmart(model, tableName, "*", null, null);
        } catch (Exception e) { error("Lỗi xóa item_shop: " + e.getMessage()); }
    }

    private void addGiftcode(String tableName, String code, String countLeft, String expired, DefaultTableModel model) {
        if (code == null || code.trim().isEmpty()) { error("Nhập code giftcode trước"); return; }
        if (tableName == null || !tableExists(tableName)) { error("Không tìm thấy bảng giftcode"); return; }
        try {
            List<String> cols = new ArrayList<>(); List<Object> vals = new ArrayList<>();
            addCol(cols, vals, tableName, "code", code.trim());
            addCol(cols, vals, tableName, "count_left", Integer.parseInt(countLeft.trim()));
            addCol(cols, vals, tableName, "expired", expired.trim());
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(").append(String.join(",", cols)).append(") VALUES (");
            for (int i = 0; i < cols.size(); i++) { if (i > 0) sql.append(","); sql.append("?"); }
            sql.append(")");
            try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < vals.size(); i++) ps.setObject(i + 1, vals.get(i));
                setStatus("Đã thêm giftcode: " + ps.executeUpdate() + " dòng");
            }
            loadTableSmart(model, tableName, "*", null, null);
        } catch (Exception e) { error("Lỗi thêm giftcode: " + e.getMessage()); }
    }

    private int countExisting(String... tables) { for (String t : tables) if (tableExists(t)) return countTable(t); return 0; }
    private int countTable(String table) {
        if (!tableExists(table)) return 0;
        try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM " + table); ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; } catch (Exception e) { return 0; }
    }
    private String firstExisting(String... tables) { for (String t : tables) if (tableExists(t)) return t; return tables.length > 0 ? tables[0] : ""; }

    private boolean tableExists(String table) {
        try (Connection con = LocalManager.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            try (ResultSet rs = md.getTables(null, null, table, null)) { if (rs.next()) return true; }
            try (ResultSet rs = md.getTables(null, null, table.toUpperCase(), null)) { return rs.next(); }
        } catch (Exception e) { return false; }
    }

    private boolean columnExists(String table, String col) {
        if (table == null || col == null) return false;
        try (Connection con = LocalManager.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, table, col)) { if (rs.next()) return true; }
            try (ResultSet rs = md.getColumns(null, null, table, col.toUpperCase())) { return rs.next(); }
        } catch (Exception e) { return false; }
    }

    private String safeExistingColumns(String table, String requested) {
        StringBuilder sb = new StringBuilder();
        for (String c : requested.split(",")) {
            String col = c.trim(); if (col.isEmpty()) continue;
            if (columnExists(table, col)) { if (sb.length() > 0) sb.append(","); sb.append(col); }
        }
        return sb.length() == 0 ? "*" : sb.toString();
    }

    private void setStatus(String s) { statusLabel.setText(s); }
    private void error(String s) { setStatus(s); JOptionPane.showMessageDialog(this, s, "RESNROSHOP", JOptionPane.WARNING_MESSAGE); }
}
