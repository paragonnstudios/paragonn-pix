package studios.paragonn.pix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import studios.paragonn.pix.domain.DonorInfo;
import studios.paragonn.pix.domain.Order;
import studios.paragonn.pix.domain.OrderProduct;
import studios.paragonn.pix.domain.PaymentInfo;
import studios.paragonn.pix.domain.PixData;
import studios.paragonn.pix.inventory.InventoryManager;
import studios.paragonn.pix.mercadopago.MercadoPagoAPI;
import studios.paragonn.pix.qrcode.ImageCreator;

public class OrderManager {
	
	private static Connection conn;
	
	protected static boolean startOrderManager(PPix ap) throws SQLException {
		FileConfiguration cfg = ap.getConfig();

		String type = cfg.getString("database.type", "auto").trim().toLowerCase();
		String autoIncrement;

		if (type.equals("mysql") || type.equals("auto")) {
			String host = cfg.getString("database.host").trim(), user = cfg.getString("database.user").trim(),
				   pass = cfg.getString("database.pass").trim(), db = cfg.getString("database.db").trim();
			int port = cfg.getInt("database.port");

			String url = "jdbc:mysql://" + host + ":" + port + "/" + db
					+ "?autoReconnect=true&characterEncoding=utf8&useSSL=false&connectTimeout=5000";
			try {
				conn = DriverManager.getConnection(url, user, pass);
				autoIncrement = "AUTO_INCREMENT";
			} catch (SQLException e) {
				if (!type.equals("auto")) {
					throw e;
				}
				Bukkit.getConsoleSender().sendMessage("§7[paragonn-pix] §eMySQL indisponivel, usando SQLite em arquivo (auto). Motivo: " + e.getMessage());
				conn = openSqlite(ap);
				autoIncrement = "AUTOINCREMENT";
			}
		}
		else if (type.equals("sqlite")) {
			conn = openSqlite(ap);
			autoIncrement = "AUTOINCREMENT";
		}
		else {
			MSG.sendMessage(Bukkit.getConsoleSender(), "db-invalido");
			return false;
		}

		conn.prepareStatement("CREATE TABLE IF NOT EXISTS autopix_orders "
				+ "(id INTEGER PRIMARY KEY " + autoIncrement + ", player VARCHAR(16) NOT NULL,"
				+ "product VARCHAR(16) NOT NULL, price DECIMAL(10, 2) NOT NULL, "
				+ "created TIMESTAMP NOT NULL, pix VARCHAR(32) UNIQUE NULL);").executeUpdate();
		
		conn.prepareStatement("CREATE TABLE IF NOT EXISTS autopix_pix_data " 
				+ "(payment_id VARCHAR(128) PRIMARY KEY, order_id INTEGER NOT NULL UNIQUE, " 
				+ "status VARCHAR(16), qr_code VARCHAR(512) NOT NULL UNIQUE);").executeUpdate();
		
		return true;
	}

	private static Connection openSqlite(PPix ap) throws SQLException {
		File flatFile = new File(ap.getDataFolder(), "autopix.db");
		Connection sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + flatFile.getAbsolutePath());
		sqliteConn.createStatement().execute("PRAGMA busy_timeout = 5000;");
		return sqliteConn;
	}

	public static Order createOrder(Player p, String product, float price) {
		try {
			
			PreparedStatement ps = conn.prepareStatement("INSERT INTO autopix_orders "
					+ "(player, product, price, created) VALUES (?, ?, ?, ?);");
			ps.setString(1, p.getName());
			ps.setString(2, product);
			ps.setFloat(3, price);
			ps.setTimestamp(4, Timestamp.from(Instant.now()));
			
			ps.executeUpdate();
			
			
			return getLastOrder(p.getName());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean savePixData(PixData pixData) {
		try {
			
			PreparedStatement ps = conn.prepareStatement("INSERT INTO autopix_pix_data "
					+ "(payment_id, order_id, status, qr_code) VALUES (?, ?, ?, ?);");
			
			ps.setString(1, pixData.getPaymentId());
			ps.setInt(2, pixData.getOrderId());
			ps.setString(3, pixData.getStatus());
			ps.setString(4, pixData.getQrCode());
			ps.executeUpdate();
			
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static List<Order> getOrders(String player) {
		return getOrders(player, 10);
	}
	
	public static List<Order> getOrders(String player, int limit) {
		List<Order> orders = new ArrayList<>();
		
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM autopix_orders WHERE player = ? " + 
														 "ORDER BY created DESC LIMIT ?;");
			ps.setString(1, player);
			ps.setInt(2, limit);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String player_real = rs.getString("player");
				String product = rs.getString("product");
				float price = rs.getFloat("price");
				Timestamp created = rs.getTimestamp("created");
				String transaction = rs.getString("pix");
				
				Order ord = new Order(player_real, product, price, created.getTime());
				ord.setId(id);
				ord.setTransaction(transaction);
				
				orders.add(ord);
			}
			
			return orders;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return orders;
	}
	
	public static Order getLastOrder(String player) {
		List<Order> orders = getOrders(player, 1);
		return orders.isEmpty() ? null : orders.get(0);
	}
	
	public static Order getOrderById(int orderId) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM autopix_orders WHERE id = ?;");
			ps.setInt(1, orderId);
			
			ResultSet rs = ps.executeQuery();
			if (!(rs.next())) return null;
			
			int id = rs.getInt("id");
			String player_real = rs.getString("player");
			String product = rs.getString("product");
			float price = rs.getFloat("price");
			Timestamp created = rs.getTimestamp("created");
			String transaction = rs.getString("pix");
			
			Order order = new Order(player_real, product, price, created.getTime());
			order.setId(id);
			order.setTransaction(transaction);
			
			return order;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static PixData getPixData(Order order) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM autopix_pix_data WHERE order_id = ?;");
			ps.setInt(1, order.getId());
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				String paymentId = rs.getString("payment_id");
				int orderId = rs.getInt("order_id");
				String status = rs.getString("status");
				String qrCode = rs.getString("qr_code");
				
				PixData pd = new PixData(paymentId, qrCode);
				pd.setOrderId(orderId);
				pd.setStatus(status);
				
				return pd;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<PixData> getAllPendingPixData() {
		List<PixData> data = new ArrayList<>();
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT * FROM autopix_pix_data WHERE status = 'pending';");
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String paymentId = rs.getString("payment_id");
				int orderId = rs.getInt("order_id");
				String status = rs.getString("status");
				String qrCode = rs.getString("qr_code");
				
				PixData pd = new PixData(paymentId, qrCode);
				pd.setOrderId(orderId);
				pd.setStatus(status);
				
				data.add(pd);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static boolean isTransactionValidated(String transactionId) {
		try {
			PreparedStatement ps = conn.prepareStatement("SELECT id FROM autopix_orders WHERE pix = ?;");
			ps.setString(1, transactionId);
			return ps.executeQuery().next();
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	public static boolean setTransaction(Order ord, String transactionId) {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE autopix_orders SET pix = ? WHERE id = ?;");
			ps.setString(1, transactionId);
			ps.setInt(2, ord.getId());
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean setPixDataStatus(PixData pixData, String status) {
		try {
			PreparedStatement ps = conn.prepareStatement("UPDATE autopix_pix_data SET status = ? WHERE payment_id = ?;");
			ps.setString(1, status);
			ps.setString(2, pixData.getPaymentId());
			ps.executeUpdate();
			pixData.setStatus(status);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	protected static void validatePendings(PPix ap) {
		for (PixData pd : getAllPendingPixData()) {
			
			Order order = getOrderById(pd.getOrderId());
			
			if (order == null) continue;
			if (order.isValidated()) continue;
			
			Player p = Bukkit.getPlayerExact(order.getPlayer());
			if (p == null) continue;
			
			OrderProduct op = InventoryManager.getProductByOrder(order);
			if (op == null) continue;
			
			PaymentInfo info = MercadoPagoAPI.getPayment(ap, pd.getPaymentId());
			if (info == null) continue;
			
			if (!setPixDataStatus(pd, info.getStatus())) continue;
			
			if (!info.getStatus().equals("approved")) continue;
			if (Math.abs(order.getPrice() - info.getPaidAmount()) > 0.001) continue;
			
			if (!setTransaction(order, info.getTransactionId())) continue;

			new BukkitRunnable() {
				@SuppressWarnings("deprecation")
				@Override
				public void run() {	
					try {
						String mapMaterial = Material.getMaterial("FILLED_MAP")!= null ? "FILLED_MAP" : "MAP";
						if (p.getItemInHand().getType().name() == mapMaterial) {
							BufferedImage gif = ImageIO.read(PPix.getInstance().getResource("success.png"));
							ItemStack successMap = ImageCreator.generateMap(gif, p, null);
							p.setItemInHand(successMap);
							
							new BukkitRunnable() {
								@Override
								public void run() {
									InventoryManager.removeUnpaidMaps(p);
								}
							}.runTaskLater(ap, 100L);
						}
					} catch (Exception e) {}
					
					if (ap.getConfig().getBoolean("som.ativar")) {
						try {
							Sound sound = Sound.valueOf(
									ap.getConfig().getString("som.efeito").toUpperCase());
							p.playSound(p.getLocation(), sound, 1, 1);
						} catch (Exception e) {}
					}
					
					for (String cmd : op.getCommands())
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
								cmd.replace("{player}", p.getName()).replace('&', '\u00a7'));
				}
			}.runTask(ap);
		}
	}
	
	public static List<DonorInfo> getTopDonors(){
		ArrayList<DonorInfo> topDonors = new ArrayList<>();
		int topLimit = PPix.getInstance().getConfig().getInt("top-doadores", 5);
		
		try {
			PreparedStatement st = conn.prepareStatement(
					"SELECT player AS donor, SUM(price) AS total "
					+ "FROM autopix_orders "
					+ "WHERE pix IS NOT NULL "
					+ "GROUP BY player "
					+ "ORDER BY total DESC "
					+ "LIMIT " + topLimit + ";");
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				topDonors.add(new DonorInfo(rs.getString("donor"), rs.getFloat("total")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return topDonors;
	}

}
