package me.gabriel.eventos.eventos.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;
import me.gabriel.eventos.api.EventoType;
import me.gabriel.eventos.api.events.PlayerLoseEvent;
import me.gabriel.eventos.eventos.Spleef;

public class SpleefListener extends EventoBaseListener {

	private EventoBaseAPI evento;

	@EventHandler
	public void onBlockBreakEventoSPLEEF(BlockBreakEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		if ((evento.isAberto()))
			return;
		if (!evento.isOcorrendo())
			return;
		Spleef spleef = (Spleef) evento;
		if (!spleef.isPodeQuebrar()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMoveEventSPLEEF(PlayerMoveEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (evento.getEventoType() == EventoType.SPLEEF)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		if (evento.isAberto())
			return;
		if (!evento.isOcorrendo())
			return;
		Spleef spleef = (Spleef) evento;
		if ((e.getPlayer().getLocation().getY() <= spleef.getY())) {
			e.getPlayer().sendMessage(spleef.getConfig().getString("Mensagens.VcFoiEliminado").replace("&", "§")
					.replace("$EventoName$", spleef.getNome()));
			PlayerLoseEvent event = new PlayerLoseEvent(e.getPlayer(), evento);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
			String msg = spleef.getConfig().getString("Mensagens.FoiEliminado");
			for (Player p : spleef.getParticipantes()) {
				p.sendMessage(msg.replace("&", "§").replace("$EventoName$", spleef.getNome()).replace("$player$",
						e.getPlayer().getName()));
			}
		}
	}
}
