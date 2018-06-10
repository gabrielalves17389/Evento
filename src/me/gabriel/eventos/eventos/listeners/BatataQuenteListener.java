package me.gabriel.eventos.eventos.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;
import me.gabriel.eventos.eventos.BatataQuente;

public class BatataQuenteListener extends EventoBaseListener {

	private EventoBaseAPI evento;

	@EventHandler
	public void onPlayerInteractEventBQUENTE(PlayerInteractEntityEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!(e.getRightClicked() instanceof Player))
			return;
		Player pa = (Player) e.getRightClicked();
		if (!evento.getParticipantes().contains(pa))
			return;
		if (evento.isAberto())
			return;
		BatataQuente batataQuente = (BatataQuente) evento;
		if (!e.getPlayer().getName().equalsIgnoreCase(batataQuente.getPlayerComBatata().getName()))
			return;
		batataQuente.setPlayerComBatata((Player) e.getRightClicked());
		e.getPlayer().getInventory().remove(new ItemStack(Material.POTATO_ITEM, 1));
		pa.getPlayer().getInventory().addItem(new ItemStack(Material.POTATO_ITEM, 1));
		for (Player p : batataQuente.getParticipantes()) {
			for (String s : evento.getConfig().getStringList("Mensagens.Esta_Com_Batata")) {
				p.sendMessage(s.replace("&", "�").replace("$player$", batataQuente.getPlayerComBatata().getName())
						.replace("$EventoName$", batataQuente.getNome()));
			}
		}
	}

	@EventHandler
	public void onInventoryClickEventBQUENTE(InventoryClickEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (evento.isAberto())
			return;
		if (!evento.getParticipantes().contains(e.getView().getPlayer()))
			return;
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItemBQUENTE(PlayerDropItemEvent a) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (evento.isAberto())
			return;
		if (!evento.getParticipantes().contains(a.getPlayer()))
			return;
		BatataQuente batataQuente = (BatataQuente) evento;
		if (a.getPlayer().getName().equalsIgnoreCase(batataQuente.getPlayerComBatata().getName())) {
			a.setCancelled(true);
		}
	}
}
