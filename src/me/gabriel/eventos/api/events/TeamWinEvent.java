package me.gabriel.eventos.api.events;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.gabriel.eventos.api.EventoBaseAPI;

public class TeamWinEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private String time;
	private EventoBaseAPI evento;
	private List<Player> time1;
	
	public TeamWinEvent(String time, EventoBaseAPI evento, List<Player> time1) {
		this.time = time;
		this.evento = evento;
		this.time1 = time1;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
		 
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public String getNomeTime() {
		return time;
	}
	
	public EventoBaseAPI getEvento() {
		return evento;
	}
	
	public List<Player> getList() {
		return time1;
	}
	
}
