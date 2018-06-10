package me.gabriel.eventos.api;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.events.PlayerLeaveEvent;
import me.gabriel.eventos.api.events.PlayerLoseEvent;

public class EventoBaseListener implements Listener {

	private EventoBaseAPI evento;

	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!Main.getEventos().getConfig().getBoolean("Bloquear_Teleporte"))
			return;
		if (evento.isAberto())
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		e.getPlayer().sendMessage(Main.getEventos().getConfigUtil().getMsgTeleporteBloqueado());
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		// Players Participantes
		if (evento.getParticipantes().contains(e.getEntity().getPlayer())) {
			PlayerLoseEvent event2 = new PlayerLoseEvent(e.getEntity().getPlayer(), evento);
			Main.getEventos().getServer().getPluginManager().callEvent(event2);
			for (Player p : evento.getParticipantes()) {
				p.sendMessage(Main.getEventos().getConfigUtil().getMsgMorreu().replace("$player$",
						e.getEntity().getPlayer().getName()));
			}
			return;
		}

		// Players Camarote
		if (evento.getCamarotePlayers().contains(e.getEntity().getPlayer())) {
			PlayerLeaveEvent event = new PlayerLeaveEvent(e.getEntity().getPlayer(), evento, true);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
			e.setNewTotalExp(e.getDroppedExp());
		}
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (evento.getParticipantes().contains(e.getPlayer())) {
			PlayerLeaveEvent event2 = new PlayerLeaveEvent(e.getPlayer(), evento, false);
			Main.getEventos().getServer().getPluginManager().callEvent(event2);
			for (Player p : evento.getParticipantes()) {
				p.sendMessage(Main.getEventos().getConfigUtil().getMsgDesconect().replace("$player$",
						e.getPlayer().getName()));
			}
		}
		// Players Camarote
		if (evento.getCamarotePlayers().contains(e.getPlayer())) {
			PlayerLeaveEvent event = new PlayerLeaveEvent(e.getPlayer(), evento, true);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
		}
	}

	@EventHandler
	public void onPlayerProccessCommandEvent(PlayerCommandPreprocessEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		for (String s : Main.getEventos().getConfig().getStringList("Comandos_Liberados")) {
			if (e.getMessage().matches("^" + s + ".*")) {
				return;
			}
		}
		e.getPlayer().sendMessage(Main.getEventos().getConfigUtil().getMsgComandoBloqueado());
		e.setCancelled(true);
	}
}
