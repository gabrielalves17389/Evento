package me.gabriel.eventos.eventos.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;
import me.gabriel.eventos.api.events.PlayerLoseEvent;
import me.gabriel.eventos.eventos.BowSpleef;

public class BowSpleefListener extends EventoBaseListener {

	private EventoBaseAPI evento;

	@EventHandler
	public void onPlayerMoveEventBSPLEEF(PlayerMoveEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		if (evento.isAberto())
			return;
		if (!(e.getFrom() != e.getTo()))
			return;
		BowSpleef bows = (BowSpleef) evento;
		if (!(e.getTo().getY() < (bows.getChao().getLowerLocation().getY() - 2)))
			return;
		e.getPlayer().sendMessage(bows.getConfig().getString("Mensagens.VcFoiEliminado").replace("&", "§")
				.replace("$EventoName$", bows.getNome()));
		PlayerLoseEvent event = new PlayerLoseEvent(e.getPlayer(), evento);
		Main.getEventos().getServer().getPluginManager().callEvent(event);
		String msg = bows.getConfig().getString("Mensagens.FoiEliminado");
		for (Player p : bows.getParticipantes()) {
			p.sendMessage(msg.replace("&", "§").replace("$EventoName$", bows.getNome()).replace("$player$",
					e.getPlayer().getName()));

		}
	}
}
