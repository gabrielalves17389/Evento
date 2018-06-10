package me.gabriel.eventos.eventos;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoCancellType;
import me.gabriel.eventos.api.EventoUtils;
import me.gabriel.eventos.api.events.PlayerWinEvent;
import me.gabriel.eventos.api.events.StopEvent;
import me.gabriel.eventos.eventos.listeners.SpleefListener;
import me.gabriel.eventos.utils.BukkitEventHelper;
import me.gabriel.eventos.utils.Cuboid;
import me.gabriel.eventos.utils.ItemStackFormat;

public class Spleef extends EventoBaseAPI {

	private SpleefListener listener;
	private boolean podeQuebrar;
	private boolean regenerarChao;
	private Cuboid cubo;
	private int tempoChaoRegenera, tempoChaoRegeneraCurrent, tempoComecar, tempoComecarCurrent;
	private int y;

	@SuppressWarnings("deprecation")
	public Spleef(YamlConfiguration config) {
		super(config);
		listener = new SpleefListener();
		Main.getEventos().getServer().getPluginManager().registerEvents(listener, Main.getEventos());
		regenerarChao = config.getBoolean("Config.Regenerar_Chao");
		tempoChaoRegenera = config.getInt("Config.Tempo_Chao_Regenera");
		tempoComecar = config.getInt("Config.Tempo_Comecar");
		podeQuebrar = false;
		tempoComecarCurrent = tempoComecar;
		y = (int) (EventoUtils.getLocation(getConfig(), "Localizacoes.Chao_1").getY() - 1.0);
		cubo = new Cuboid(EventoUtils.getLocation(getConfig(), "Localizacoes.Chao_1"),
				EventoUtils.getLocation(getConfig(), "Localizacoes.Chao_2"));
		for (Block b : cubo.getBlocks()) {
			b.setType(Material.getMaterial(getConfig().getInt("Config.Chao_ID")));
		}
	}

	@Override
	public void startEventMethod() {
		for (Player p : getParticipantes()) {
			for (String linha : getConfig().getStringList("Itens_Ao_Iniciar")) {
				p.getInventory().addItem(new ItemStack(ItemStackFormat.getItem(linha)));
			}
			p.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Entrada"));
			for (String s1 : getConfig().getStringList("Mensagens.IniciandoEm")) {
				p.sendMessage(s1.replace("&", "§").replace("$tempo$", String.valueOf(tempoComecar))
						.replace("$EventoName$", getNome()));
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void scheduledMethod() {
		if (tempoComecarCurrent == 0 && !podeQuebrar) {
			for (Player p : getParticipantes()) {
				for (String s1 : getConfig().getStringList("Mensagens.Pode_Quebrar")) {
					p.sendMessage(s1.replace("&", "§").replace("$tempo$", String.valueOf(tempoComecar))
							.replace("$EventoName$", getNome()));
				}
			}
			podeQuebrar = true;
		} else if (!podeQuebrar) {
			tempoComecarCurrent--;
		}
		if (regenerarChao) {
			if (tempoChaoRegeneraCurrent == 0) {
				for (Block b : cubo.getBlocks()) {
					b.setType(Material.getMaterial(getConfig().getInt("Config.Chao_ID")));
				}
				tempoChaoRegeneraCurrent = tempoChaoRegenera;
			} else {
				tempoChaoRegeneraCurrent--;
			}
		}
		if (getParticipantes().size() == 1) {
			Player player = getParticipantes().get(0);
			PlayerWinEvent event = new PlayerWinEvent(player, this, false);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
			stopEvent();
		}
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

	public boolean isPodeQuebrar() {
		return this.podeQuebrar;
	}

	public double getY() {
		return this.y;
	}

	public int getTempoComecarCurrent() {
		return this.tempoComecarCurrent;
	}
}
