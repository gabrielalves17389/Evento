package me.gabriel.eventos.eventos.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoBaseListener;
import me.gabriel.eventos.api.events.PlayerLoseEvent;
import me.gabriel.eventos.eventos.Fight;

public class FightListener extends EventoBaseListener {

	private EventoBaseAPI evento;

	@EventHandler
	public void onEntityDamageByEntityEventFIGHT(EntityDamageByEntityEvent e) {
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
		Fight fight = (Fight) evento;
		if (!evento.getParticipantes().contains(p))
			return;
		if (fight.getLutador1() == e.getEntity() || fight.getLutador2() == e.getEntity()) {
			return;
		}
		if (fight.getLutador1() == e.getDamager() || fight.getLutador2() == e.getDamager()) {
			return;
		}
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerQuitEventFIGHT(PlayerQuitEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getPlayer()))
			return;
		if (!evento.isOcorrendo())
			return;
		if (evento.isAberto())
			return;
		Fight fight = (Fight) evento;
		if (fight.getLutador1() != e.getPlayer() || fight.getLutador2() != e.getPlayer()) {
			return;
		}
		if (fight.getLutador1() == e.getPlayer()) {
			PlayerLoseEvent event = new PlayerLoseEvent(e.getPlayer(), evento);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
			fight.venceuLuta(fight.getLutador2());
			fight.taskLuta().cancel();
		} else if (fight.getLutador2() == e.getPlayer()) {
			PlayerLoseEvent event = new PlayerLoseEvent(e.getPlayer(), fight);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
			fight.venceuLuta(fight.getLutador1());
			fight.taskLuta().cancel();
		}
		fight.setLutaOcorrendo(false);
	}

	@EventHandler
	public void onPlayerDeathEventFIGHT(PlayerDeathEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		if (evento == null)
			return;
		if (!evento.getParticipantes().contains(e.getEntity()))
			return;
		if (!evento.isOcorrendo())
			return;
		if (evento.isAberto())
			return;
		Fight fight = (Fight) evento;
		if (fight.getLutador1() == e.getEntity().getPlayer() || fight.getLutador2() == e.getEntity().getPlayer()) {
			fight.venceuLuta(e.getEntity().getKiller());
			fight.taskLuta().cancel();
			fight.setLutaOcorrendo(false);
		}
	}

	@EventHandler
	public void onPlayerLoseEventFIGHT(PlayerLoseEvent e) {
		evento = Main.getEventos().getEventosController().getEvento();
		Fight fight = (Fight) evento;
		if (fight.getPrimeiraRodada().contains(e.getPlayer())) {
			fight.getPrimeiraRodada().remove(e.getPlayer());
		} else if (fight.getSegundaRodada().contains(e.getPlayer())) {
			fight.getSegundaRodada().remove(e.getPlayer());
		}
		if (Main.getEventos().getSc() != null) {
			fight.getClans()
					.remove(Main.getEventos().getSc().getClanManager().getClanPlayer(e.getPlayer()));
			Main.getEventos().getSc().getClanManager().getClanPlayer(e.getPlayer()).setFriendlyFire(false);
		}
	}
}
