package me.gabriel.eventos.eventos;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoCancellType;
import me.gabriel.eventos.api.EventoUtils;
import me.gabriel.eventos.api.events.StopEvent;
import me.gabriel.eventos.eventos.listeners.MinaMortalListener;
import me.gabriel.eventos.utils.BukkitEventHelper;
import me.gabriel.eventos.utils.Cuboid;

public class MinaMortal extends EventoBaseAPI {

	private MinaMortalListener listener;
	private int tempoDeEvento, tempoDeEventoCurrent, tempoMensagens, tempoMensagensCurrent;

	@SuppressWarnings("deprecation")
	public MinaMortal(YamlConfiguration config) {
		super(config);
		listener = new MinaMortalListener();
		Main.getEventos().getServer().getPluginManager().registerEvents(listener, Main.getEventos());
		tempoDeEvento = config.getInt("Config.Evento_Tempo_Minutos") * 60;
		tempoMensagens = config.getInt("Config.Mensagens_Tempo_Minutos") * 60;
		tempoDeEventoCurrent = tempoDeEvento;
		tempoMensagensCurrent = tempoMensagens;
		Cuboid cubo = new Cuboid(EventoUtils.getLocation(getConfig(), "Localizacoes.Mina_1"),
				EventoUtils.getLocation(getConfig(), "Localizacoes.Mina_2"));
		ArrayList<String> blocosConfig = new ArrayList<>();
		for (String s : getConfig().getString("Config.Minerios").split(";")) {
			blocosConfig.add(s);
		}
		for (Block b : cubo.getBlocks()) {
			Random r = new Random();
			if (r.nextInt(100) <= getConfig().getInt("Config.Porcentagem_De_Minerios")) {
				String bloco = blocosConfig.get(r.nextInt(blocosConfig.size()));
				b.setType(Material.getMaterial(Integer.parseInt(bloco)));
			} else {
				b.setType(Material.STONE);
			}
		}
	}

	@Override
	public void startEventMethod() {
		for (Player p : getParticipantes()) {
			p.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Entrada"));
		}
	}

	@Override
	public void scheduledMethod() {
		if (getParticipantes().size() >= 1) {
			if (tempoDeEventoCurrent > 0) {
				tempoDeEventoCurrent--;
				if (tempoMensagensCurrent == 0) {
					for (String s : getConfig().getStringList("Mensagens.Status")) {
						Main.getEventos().getServer().broadcastMessage(s.replace("&", "§")
								.replace("$tempo$", tempoDeEventoCurrent + "").replace("$EventoName$", getNome()));
					}
					tempoMensagensCurrent = tempoMensagens;
				} else {
					tempoMensagensCurrent--;
				}
			} else {
				stopEvent();
			}
		} else {
			stopEvent();
		}
	}

	@Override
	public void stopEventMethod() {
		sendMessageList("Mensagens.Finalizado");
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
		super.resetEvent();
		BukkitEventHelper.unregisterEvents(listener, Main.getEventos());
	}
}
