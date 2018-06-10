package me.gabriel.eventos.eventos;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoCancellType;
import me.gabriel.eventos.api.EventoUtils;
import me.gabriel.eventos.api.events.StopEvent;
import me.gabriel.eventos.eventos.listeners.EventoNormalListener;
import me.gabriel.eventos.utils.BukkitEventHelper;

public class EventoNormal extends EventoBaseAPI {

	private EventoNormalListener listener;

	public EventoNormal(YamlConfiguration config) {
		super(config);
		listener = new EventoNormalListener();
		Main.getEventos().getServer().getPluginManager().registerEvents(listener, Main.getEventos());
	}

	@Override
	public void startEventMethod() {
		for (Player s : getParticipantes()) {
			s.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Entrada"));
		}
	}

	@Override
	public void cancelEventMethod() {
		sendMessageList("Mensagens.Cancelado");
	}

	@Override
	public void stopEvent() {
		super.stopEvent();
		StopEvent event = new StopEvent(Main.getEventos().getEventosController().getEvento(),
				EventoCancellType.FINISHED);
		Main.getEventos().getServer().getPluginManager().callEvent(event);
	}

	@Override
	public void resetEvent() {
		super.resetEvent();
		BukkitEventHelper.unregisterEvents(listener, Main.getEventos());
	}
}
