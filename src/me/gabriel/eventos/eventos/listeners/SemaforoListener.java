package me.gabriel.eventos.eventos.listeners;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;
import me.gabriel.eventos.api.events.PlayerLoseEvent;
import me.gabriel.eventos.api.events.PlayerWinEvent;
import me.gabriel.eventos.eventos.Semaforo;

public class SemaforoListener extends EventoBaseListener {

	private EventoBaseAPI evento;

	@EventHandler
	public void onPlayerInteractEventSEMAFORO(PlayerInteractEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		if (!evento.isOcorrendo())
			return;
		if (evento.isAberto())
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if ((e.getClickedBlock().getType() == Material.SIGN_POST)
					|| (e.getClickedBlock().getType() == Material.WALL_SIGN)) {
				Sign s = (Sign) e.getClickedBlock().getState();
				if (s.getLine(0).equalsIgnoreCase("�9[Evento]")) {
					PlayerWinEvent event = new PlayerWinEvent(e.getPlayer(), evento, false);
					Main.getEventos().getServer().getPluginManager().callEvent(event);
					Main.getEventos().getEventosController().getEvento().stopEvent();
					s.setLine(1, "�6" + e.getPlayer().getName());
					s.update();
				}
			}
		}

	}

	@EventHandler
	public void onPlayerMoveEventSEMAFORO(PlayerMoveEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		if (!evento.isOcorrendo())
			return;
		if (evento.isAberto())
			return;
		if ((e.getFrom().getX() != e.getTo().getX()) && (e.getFrom().getZ() != e.getTo().getZ())) {
			Semaforo semaforo = (Semaforo) evento;
			if (!semaforo.isPodeAndar()) {
				e.getPlayer().sendMessage(semaforo.getConfig().getString("Mensagens.VcFoiEliminado").replace("&", "�")
						.replace("$EventoName$", semaforo.getNome()));
				PlayerLoseEvent event = new PlayerLoseEvent(e.getPlayer(), evento);
				Main.getEventos().getServer().getPluginManager().callEvent(event);
				String msg = semaforo.getConfig().getString("Mensagens.FoiEliminado");
				for (Player p : semaforo.getParticipantes()) {
					p.sendMessage(msg.replace("&", "�").replace("$EventoName$", semaforo.getNome()).replace("$player$",
							e.getPlayer().getName()));
				}
			}
		}
	}
}
