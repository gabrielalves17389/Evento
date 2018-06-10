package me.gabriel.eventos.eventos.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;
import me.gabriel.eventos.api.events.PlayerWinEvent;

public class EventoNormalListener extends EventoBaseListener {

	private EventoBaseAPI evento;

	@EventHandler
	public void onPlayerInteractEventNORMAL(PlayerInteractEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		if (!evento.isOcorrendo())
			return;
		if ((evento.isAberto()))
			return;
		if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK))
			return;
		if ((e.getClickedBlock().getType() == Material.SIGN_POST)
				|| (e.getClickedBlock().getType() == Material.WALL_SIGN)) {
			Sign s = (Sign) e.getClickedBlock().getState();
			if (s.getLine(0).equalsIgnoreCase("§9[Evento]")) {
				if (evento.getParticipantes().size() >= 1) {
					PlayerWinEvent event = new PlayerWinEvent(e.getPlayer(), evento, false);
					Main.getEventos().getServer().getPluginManager().callEvent(event);
					evento.stopEvent();
					s.setLine(1, "§6" + e.getPlayer().getName());
					s.update();
				}
			}
		}
	}
}
