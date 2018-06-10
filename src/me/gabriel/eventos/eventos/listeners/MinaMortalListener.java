package me.gabriel.eventos.eventos.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;

public class MinaMortalListener extends EventoBaseListener {

	private EventoBaseAPI evento;
	
	@EventHandler
	public void onPotionSplashEventMINA(PotionSplashEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento != null) {
			if (e.getPotion().getShooter() instanceof Player) {
				Player p = (Player) e.getPotion().getShooter();
				if (evento.getCamarotePlayers().contains(p)) {
					e.setCancelled(true);
				}
			}
		}
	}
}
