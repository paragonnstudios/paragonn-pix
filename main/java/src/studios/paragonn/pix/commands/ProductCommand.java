package studios.paragonn.pix.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import studios.paragonn.pix.MSG;
import studios.paragonn.pix.inventory.InventoryListener;
import studios.paragonn.pix.inventory.InventoryManager;

public class ProductCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cEste comando só pode ser usado in-game.");
			return true;
		}
		
		Player p = (Player) sender;
		if (!(p.hasPermission("autopix.use"))) {
			MSG.sendMessage(p, "sem-permissao");
			return true;
		}
		
		if (args.length == 0) {
			p.sendMessage("§cUse: /produto <produto>");
			return true;
		}
		
		String productName = args[0];
		Object[] menuAndSlot = InventoryManager.getProductMenuAndSlot(productName);
		
		if (menuAndSlot == null) {
			p.sendMessage("§cProduto não encontrado.");
			return true;
		}
		
		String menu = (String) menuAndSlot[0];
		int slot = (int) menuAndSlot[1];
		
		InventoryListener.setBuyingState(p, menu, slot);
		InventoryManager.openConfirmation(p);
		
		return true;
	}

}
