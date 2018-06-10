package me.gabriel.eventos.eventos.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;
import me.gabriel.eventos.api.events.PlayerLoseEvent;
import me.gabriel.eventos.eventos.Killer;

public class KillerListener extends EventoBaseListener {

	EventoBaseAPI evento;
	
	@EventHandler
	public void onEntityDamageByEntityEventKILLER(EntityDamageByEntityEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!(e.getDamager() instanceof Player))
			return;
		if (!evento.isOcorrendo())
			return;
		if (evento.isAberto())
			return;
		Player p = (Player) e.getDamager();
		Killer killer = (Killer) evento;
		if (!evento.getParticipantes().contains(p))
			return;
		if (killer.getEtapa() != 1)
			return;
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuitEventKILLER(PlayerQuitEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.isOcorrendo())
			return;
		if (evento.getParticipantes()
				.contains(e.getPlayer()))
			return;
		if (evento.isAberto())
			return;
		e.getPlayer().setHealth(0.0);
	}

	@EventHandler
	public void onPotionSplashEventKILLER(PotionSplashEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento != null) {
			if (e.getPotion().getShooter() instanceof Player) {
				Player p = (Player) e.getPotion().getShooter();
				if (evento.getCamarotePlayers()
						.contains(p)) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerLoseEventKILLER(PlayerLoseEvent e) {
		Killer killer = (Killer) evento;
		if (Main.getEventos().getSc() != null) {
			killer.getClans().remove(Main.getEventos().getSc().getClanManager().getClanPlayer(e.getPlayer()));
			Main.getEventos().getSc().getClanManager().getClanPlayer(e.getPlayer()).setFriendlyFire(false);
		}
	}
}
