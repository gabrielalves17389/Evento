package me.gabriel.eventos.eventos;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoCancellType;
import me.gabriel.eventos.api.EventoUtils;
import me.gabriel.eventos.api.events.StopEvent;
import me.gabriel.eventos.eventos.listeners.SemaforoListener;
import me.gabriel.eventos.utils.BukkitEventHelper;

public class Semaforo extends EventoBaseAPI {

	private SemaforoListener listener;
	private int tempoAmarelo, tempoVerde, tempoVermelho;
	private BukkitTask verde, amarelo, vermelho;
	private boolean podeAndar;

	public Semaforo(YamlConfiguration config) {
		super(config);
		listener = new SemaforoListener();
		Main.getEventos().getServer().getPluginManager().registerEvents(listener, Main.getEventos());
		tempoVerde = config.getInt("Config.Tempo_Verde");
		tempoAmarelo = config.getInt("Config.Tempo_Amarelo");
		tempoVermelho = config.getInt("Config.Tempo_Vermelho");
		podeAndar = true;
	}

	@Override
	public void startEventMethod() {
		for (Player p : getParticipantes()) {
			p.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Entrada"));
		}
		semaforoMethod();
	}

	@Override
	public void scheduledMethod() {
		if (getParticipantes().size() == 0) {
			sendMessageList("Mensagens.Sem_Vencedor");
			stopEvent();
		}
	}

	public void semaforoMethod() {
		podeAndar = true;
		for (Player p : getParticipantes()) {
			for (String msg : getConfig().getStringList("Mensagens.Verde")) {
				p.sendMessage(msg.replace("&", "§").replace("$EventoName$", getNome()));
			}
		}
		verde = Main.getEventos().getServer().getScheduler().runTaskLater(Main.getEventos(), new Runnable() {
			@Override
			public void run() {
				for (Player p : getParticipantes()) {
					for (String msg : getConfig().getStringList("Mensagens.Amarelo")) {
						p.sendMessage(msg.replace("&", "§").replace("$EventoName$", getNome()));
					}
				}
				amarelo = Main.getEventos().getServer().getScheduler().runTaskLater(Main.getEventos(),
						new Runnable() {
							@Override
							public void run() {
								podeAndar = false;
								for (Player p : getParticipantes()) {
									for (String msg : getConfig().getStringList("Mensagens.Vermelho")) {
										p.sendMessage(msg.replace("&", "§").replace("$EventoName$", getNome()));
									}
								}
								vermelho = Main.getEventos().getServer().getScheduler()
										.runTaskLater(Main.getEventos(), new Runnable() {
											@Override
											public void run() {
												semaforoMethod();
											}
										}, tempoVermelho * 20L);
							}
						}, tempoAmarelo * 20L);
			}
		}, tempoVerde * 20L);
	}

	@Override
	public void cancelEventMethod() {
		sendMessageList("Mensagens.Cancelado");
	}

	@Override
	public void stopEvent() {
		StopEvent event = new StopEvent(Main.getEventos().getEventosController().getEvento(),
				EventoCancellType.FINISHED);
		Main.getEventos().getServer().getPluginManager().callEvent(event);
	}

	@Override
	public void resetEvent() {
		if (verde != null)
			verde.cancel();
		if (amarelo != null)
			amarelo.cancel();
		if (vermelho != null) {
			vermelho.cancel();
		}
		super.resetEvent();
		BukkitEventHelper.unregisterEvents(listener, Main.getEventos());
	}

	public boolean isPodeAndar() {
		return this.podeAndar;
	}

}
