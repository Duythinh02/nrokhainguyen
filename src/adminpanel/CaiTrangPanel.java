package adminpanel;

import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import nro.models.data.DataGame;
import nro.models.data.LocalManager;

/**
 * Quản lý cải trang: danh sách mọi cải trang (item_template TYPE = 5), tìm kiếm, xem hình và sửa thông tin.
 */
public class CaiTrangPanel extends JPanel {

    private static final String[] GENDERS = {"Trái Đất", "Namếc", "Xayda", "Chung"};

    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Tất cả hành tinh", "Trái Đất", "Namếc", "Xayda", "Chung"});
    private final JLabel lblCount = new JLabel("0 cải trang");

    private final JTextField tfId = new JTextField();
    private final JTextField tfName = new JTextField();
    private final JTextArea taDesc = new JTextArea(3, 20);
    private final JComboBox<String> cbEditGender = new JComboBox<>(GENDERS);
    private final JTextField tfHead = new JTextField();
    private final JTextField tfBody = new JTextField();
    private final JTextField tfLeg = new JTextField();
    private final JTextField tfIcon = new JTextField();
    private final JTextField tfPower = new JTextField();
    private final JLabel lblIcon = new JLabel();
    private final JLabel lblHead = new JLabel();

    private final Map<Integer, ImageIcon> iconCache = new HashMap<>();
    private final Map<Integer, Integer> headFrameIcon = new HashMap<>();

    public CaiTrangPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Thanh tìm kiếm
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JLabel title = new JLabel("Quản Lý Cải Trang");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(title, BorderLayout.WEST);
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBar.setOpaque(false);
        txtSearch.setPreferredSize(new Dimension(220, 28));
        txtSearch.setToolTipText("Nhập tên hoặc ID cải trang");
        JButton btnSearch = new JButton("Tìm");
        JButton btnReload = new JButton("Làm mới");
        searchBar.add(new JLabel("Tên/ID:"));
        searchBar.add(txtSearch);
        searchBar.add(cbGender);
        searchBar.add(btnSearch);
        searchBar.add(btnReload);
        searchBar.add(lblCount);
        top.add(searchBar, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Bảng danh sách
        model = new DefaultTableModel(new Object[]{"ID", "Icon", "Tên", "Hành tinh", "Đầu", "Thân", "Chân", "SM yêu cầu"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 1 ? ImageIcon.class : Object.class;
            }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.setAutoCreateRowSorter(true);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(buildDetailPanel(), BorderLayout.EAST);

        btnSearch.addActionListener(e -> loadData());
        btnReload.addActionListener(e -> {
            iconCache.clear();
            headFrameIcon.clear();
            loadData();
        });
        txtSearch.addActionListener(e -> loadData());
        cbGender.addActionListener(e -> loadData());

        loadData();
    }

    private JPanel buildDetailPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setPreferredSize(new Dimension(330, 0));
        p.setBorder(BorderFactory.createTitledBorder("Chi tiết cải trang"));

        JPanel imgs = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 4));
        lblIcon.setPreferredSize(new Dimension(70, 70));
        lblHead.setPreferredSize(new Dimension(70, 70));
        lblIcon.setBorder(BorderFactory.createTitledBorder("Icon"));
        lblHead.setBorder(BorderFactory.createTitledBorder("Đầu"));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblHead.setHorizontalAlignment(SwingConstants.CENTER);
        imgs.add(lblIcon);
        imgs.add(lblHead);
        p.add(imgs, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        tfId.setEditable(false);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        Object[][] rows = {
            {"ID", tfId}, {"Tên", tfName}, {"Mô tả", new JScrollPane(taDesc)}, {"Hành tinh", cbEditGender},
            {"Part đầu", tfHead}, {"Part thân", tfBody}, {"Part chân", tfLeg}, {"Icon ID", tfIcon}, {"SM yêu cầu", tfPower}
        };
        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0;
            form.add(new JLabel((String) rows[i][0]), g);
            g.gridx = 1;
            g.weightx = 1;
            form.add((Component) rows[i][1], g);
        }
        p.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(e -> saveSelected());
        JLabel note = new JLabel("<html><i>Sửa xong cần khởi động lại server và tăng vsItem (DataGame) để client nhận tên/hình mới.</i></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bottom.add(btnSave, BorderLayout.NORTH);
        bottom.add(note, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        tfIcon.addActionListener(e -> refreshPreview());
        tfHead.addActionListener(e -> refreshPreview());
        return p;
    }

    private Connection getConnection() throws SQLException {
        return LocalManager.getConnection();
    }

    private void loadData() {
        String keyword = txtSearch.getText().trim();
        int genderFilter = cbGender.getSelectedIndex() - 1; // -1 = tất cả
        new Thread(() -> {
            List<Object[]> rows = new ArrayList<>();
            String sql = "SELECT id, NAME, gender, icon_id, head, body, leg, power_require FROM item_template WHERE TYPE = 5"
                    + (genderFilter >= 0 ? " AND gender = ?" : "")
                    + (keyword.isEmpty() ? "" : " AND (NAME LIKE ? OR id = ?)")
                    + " ORDER BY id";
            try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
                int i = 1;
                if (genderFilter >= 0) {
                    ps.setInt(i++, genderFilter);
                }
                if (!keyword.isEmpty()) {
                    ps.setString(i++, "%" + keyword + "%");
                    int idKey = -999;
                    try {
                        idKey = Integer.parseInt(keyword);
                    } catch (NumberFormatException ignored) {
                    }
                    ps.setInt(i, idKey);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int gender = rs.getInt("gender");
                        rows.add(new Object[]{
                            rs.getInt("id"), getIcon(rs.getInt("icon_id")), rs.getString("NAME"),
                            gender >= 0 && gender < GENDERS.length ? GENDERS[gender] : String.valueOf(gender),
                            rs.getInt("head"), rs.getInt("body"), rs.getInt("leg"), String.format("%,d", rs.getLong("power_require"))
                        });
                    }
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi tải cải trang: " + ex.getMessage()));
                return;
            }
            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                for (Object[] r : rows) {
                    model.addRow(r);
                }
                lblCount.setText(rows.size() + " cải trang");
            });
        }, "CaiTrangPanel-load").start();
    }

    private void showSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int id = (int) model.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        new Thread(() -> {
            try (Connection con = getConnection();
                    PreparedStatement ps = con.prepareStatement("SELECT id, NAME, description, gender, icon_id, head, body, leg, power_require FROM item_template WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("NAME");
                        String desc = rs.getString("description");
                        int gender = rs.getInt("gender");
                        int icon = rs.getInt("icon_id");
                        int head = rs.getInt("head");
                        int body = rs.getInt("body");
                        int leg = rs.getInt("leg");
                        long power = rs.getLong("power_require");
                        SwingUtilities.invokeLater(() -> {
                            tfId.setText(String.valueOf(id));
                            tfName.setText(name);
                            taDesc.setText(desc);
                            cbEditGender.setSelectedIndex(gender >= 0 && gender < GENDERS.length ? gender : 3);
                            tfHead.setText(String.valueOf(head));
                            tfBody.setText(String.valueOf(body));
                            tfLeg.setText(String.valueOf(leg));
                            tfIcon.setText(String.valueOf(icon));
                            tfPower.setText(String.valueOf(power));
                            refreshPreview();
                        });
                    }
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi đọc cải trang: " + ex.getMessage()));
            }
        }, "CaiTrangPanel-detail").start();
    }

    private void refreshPreview() {
        try {
            lblIcon.setIcon(getIconBig(Integer.parseInt(tfIcon.getText().trim())));
        } catch (Exception e) {
            lblIcon.setIcon(null);
        }
        try {
            int head = Integer.parseInt(tfHead.getText().trim());
            new Thread(() -> {
                ImageIcon img = getIconBig(getHeadFrameIcon(head));
                SwingUtilities.invokeLater(() -> lblHead.setIcon(img));
            }, "CaiTrangPanel-head").start();
        } catch (Exception e) {
            lblHead.setIcon(null);
        }
    }

    private void saveSelected() {
        if (tfId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn một cải trang trong danh sách trước.");
            return;
        }
        int id = Integer.parseInt(tfId.getText());
        int head, body, leg, icon;
        long power;
        try {
            head = Integer.parseInt(tfHead.getText().trim());
            body = Integer.parseInt(tfBody.getText().trim());
            leg = Integer.parseInt(tfLeg.getText().trim());
            icon = Integer.parseInt(tfIcon.getText().trim());
            power = Long.parseLong(tfPower.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Part, Icon ID và SM yêu cầu phải là số.");
            return;
        }
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên không được để trống.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Lưu thay đổi cho cải trang ID " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String desc = taDesc.getText();
        int gender = cbEditGender.getSelectedIndex();
        new Thread(() -> {
            try (Connection con = getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE item_template SET NAME = ?, description = ?, gender = ?, head = ?, body = ?, leg = ?, icon_id = ?, power_require = ? WHERE id = ? AND TYPE = 5")) {
                ps.setString(1, name);
                ps.setString(2, desc);
                ps.setInt(3, gender);
                ps.setInt(4, head);
                ps.setInt(5, body);
                ps.setInt(6, leg);
                ps.setInt(7, icon);
                ps.setLong(8, power);
                ps.setInt(9, id);
                int n = ps.executeUpdate();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, n > 0 ? "Đã lưu cải trang ID " + id + "." : "Không lưu được (không phải cải trang?).");
                    loadData();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi lưu: " + ex.getMessage()));
            }
        }, "CaiTrangPanel-save").start();
    }

    // Icon frame đầu tiên của part đầu (bảng part, DATA = [[iconId,dx,dy],...])
    private int getHeadFrameIcon(int headPart) {
        Integer cached = headFrameIcon.get(headPart);
        if (cached != null) {
            return cached;
        }
        int iconId = -1;
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement("SELECT DATA FROM part WHERE id = ?")) {
            ps.setInt(1, headPart);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Matcher m = Pattern.compile("\\[(\\d+),").matcher(rs.getString(1));
                    if (m.find()) {
                        iconId = Integer.parseInt(m.group(1));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        headFrameIcon.put(headPart, iconId);
        return iconId;
    }

    private ImageIcon getIcon(int iconId) {
        if (iconId < 0) {
            return null;
        }
        if (iconCache.containsKey(iconId)) {
            return iconCache.get(iconId);
        }
        ImageIcon icon = null;
        try {
            File f = DataGame.getIconFile(iconId);
            if (f.exists()) {
                icon = new ImageIcon(ImageIO.read(f).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        iconCache.put(iconId, icon);
        return icon;
    }

    private ImageIcon getIconBig(int iconId) {
        if (iconId < 0) {
            return null;
        }
        try {
            File f = DataGame.getIconFile(iconId);
            if (f.exists()) {
                return new ImageIcon(ImageIO.read(f).getScaledInstance(56, 56, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
