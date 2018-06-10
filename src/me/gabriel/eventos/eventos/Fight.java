package me.gabriel.eventos.eventos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import me.gabriel.eventos.Main;
import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoCancellType;
import me.gabriel.eventos.api.EventoUtils;
import me.gabriel.eventos.api.events.PlayerLoseEvent;
import me.gabriel.eventos.api.events.PlayerWinEvent;
import me.gabriel.eventos.api.events.StopEvent;
import me.gabriel.eventos.eventos.listeners.FightListener;
import me.gabriel.eventos.utils.BukkitEventHelper;
import me.gabriel.eventos.utils.ItemStackFormat;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;

public class Fight extends EventoBaseAPI {

	private FightListener listener;
	private boolean lutaOcorrendo;
	private Player lutador1, lutador2;
	private List<ClanPlayer> clans = new ArrayList<ClanPlayer>();
	private List<Player> rodada = new ArrayList<>();
	private List<Player> rodadaProxima = new ArrayList<>();
	private BukkitTask taskAvisos, taskLuta;

	public Fight(YamlConfiguration config) {
		super(config);
		listener = new FightListener();
		Main.getEventos().getServer().getPluginManager().registerEvents(listener, Main.getEventos());
		lutaOcorrendo = false;
		rodada.clear();
		rodadaProxima.clear();
	}

	@Override
	public void startEventMethod() {
		for (Player p : getParticipantes()) {
			p.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Entrada"));
			if (Main.getEventos().getSc() != null) {
				if (Main.getEventos().getSc().getClanManager().getClanPlayer(p) != null) {
					Main.getEventos().getSc().getClanManager().getClanPlayer(p).setFriendlyFire(true);
					clans.add(Main.getEventos().getSc().getClanManager().getClanPlayer(p));
				}
			}
		}
		statusAvisos();
		rodada.addAll(getParticipantes());
	}

	@Override
	public void scheduledMethod() {
		if (getParticipantes().size() > 2) {
			if (rodada.size() > 2) {
				if (!lutaOcorrendo) {
					iniciarLuta();
				}
			} else if (rodada.size() == 1) {
				Player p1 = rodada.get(0);
				rodadaProxima.add(p1);
				rodada.remove(p1);
				p1.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Entrada"));
			} else if (rodada.size() == 0) {
				rodada.addAll(rodadaProxima);
				rodadaProxima.clear();
			}
		} else if (getParticipantes().size() == 2) {
			if (!lutaOcorrendo) {
				iniciarLuta();
			}
		} else if (getParticipantes().size() == 1) {
			PlayerWinEvent event = new PlayerWinEvent(getParticipantes().get(0),
					Main.getEventos().getEventosController().getEvento(), false);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
			stopEvent();
		} else if (getParticipantes().size() == 0) {
			sendMessageList("Mensagens.Sem_Vencedor");
			stopEvent();
		}
	}

	public void statusAvisos() {
		taskAvisos = Main.getEventos().getServer().getScheduler().runTaskTimer(Main.getEventos(),
				new Runnable() {
					@Override
					public void run() {
						for (String s : getConfig().getStringList("Mensagens.Status")) {
							Main.getEventos().getServer()
									.broadcastMessage(s.replace("&", "§")
											.replace("$jogadores$", String.valueOf(getParticipantes().size()))
											.replace("$EventoName$", getNome()));
						}
					}
				}, 0L, getConfig().getInt("Config.Tempo_Entre_Avisos") * 20L);
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
		super.stopEvent();
	}

	@Override
	public void resetEvent() {
		if (taskAvisos != null) {
			taskAvisos.cancel();
		}
		if (Main.getEventos().getSc() != null) {
			for (ClanPlayer string : clans) {
				string.setFriendlyFire(false);
			}
		}
		super.resetEvent();
		BukkitEventHelper.unregisterEvents(listener, Main.getEventos());
	}

	public void iniciarLuta() {
		lutaOcorrendo = true;
		lutador1 = null;
		lutador2 = null;
		for (Player p : getParticipantes()) {
			for (String s1 : getConfig().getStringList("Mensagens.Iniciando_Em")) {
				p.sendMessage(s1.replace("&", "§")
						.replace("$tempo$", String.valueOf(getConfig().getInt("Config.Tempo_Entre_Lutas")))
						.replace("$EventoName$", getNome()));
			}
		}
		Main.getEventos().getServer().getScheduler().runTaskLater(Main.getEventos(), new Runnable() {
			public void run() {
				Random random = new Random();
				if (getParticipantes().size() > 2) {
					do {
						lutador1 = rodada.get(random.nextInt(rodada.size()));
						lutador2 = rodada.get(random.nextInt(rodada.size()));
					} while (lutador1 == lutador2);
					adicionarItens(lutador1, false);
					adicionarItens(lutador2, false);
				} else if (getParticipantes().size() == 2) {
					lutador1 = getParticipantes().get(0);
					lutador2 = getParticipantes().get(1);
					adicionarItens(lutador1, true);
					adicionarItens(lutador2, true);
				}
				for (Player p : getParticipantes()) {
					for (String s1 : getConfig().getStringList("Mensagens.Iniciar_Luta")) {
						p.sendMessage(s1.replace("&", "§").replace("$EventoName$", getNome())
								.replace("$lutador1$", lutador1.getName()).replace("$lutador2$", lutador2.getName()));
					}
				}
				lutador1.setHealth(lutador1.getMaxHealth());
				lutador2.setHealth(lutador2.getMaxHealth());
				lutador1.setFoodLevel(20);
				lutador2.setFoodLevel(20);
				lutador1.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Pos_1"));
				lutador2.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Pos_2"));
				taskLuta = Main.getEventos().getServer().getScheduler().runTaskLater(Main.getEventos(),
						new Runnable() {
							@Override
							public void run() {
								perdeuLuta(lutador1);
								perdeuLuta(lutador2);
								for (Player p : getParticipantes()) {
									for (String s1 : getConfig().getStringList("Mensagens.Luta_Sem_Vencedor")) {
										p.sendMessage(s1.replace("&", "§").replace("$EventoName$", getNome()));
									}
								}
								lutaOcorrendo = false;
							}
						}, getConfig().getInt("Config.Tempo_Luta") * 20L);
			}
		}, getConfig().getInt("Config.Tempo_Entre_Lutas") * 20L);
	}

	public void venceuLuta(Player p) {
		rodadaProxima.add(p);
		rodada.remove(p);
		p.teleport(EventoUtils.getLocation(getConfig(), "Localizacoes.Entrada"));
		for (Player p1 : getParticipantes()) {
			for (String s1 : getConfig().getStringList("Mensagens.Luta_Vencedor")) {
				p1.sendMessage(
						s1.replace("&", "§").replace("$EventoName$", getNome()).replace("$vencedor$", p.getName()));
			}
		}
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}

	public void perdeuLuta(Player p) {
		PlayerLoseEvent event = new PlayerLoseEvent(p, Main.getEventos().getEventosController().getEvento());
		Main.getEventos().getServer().getPluginManager().callEvent(event);
	}

	private void adicionarItens(Player p, boolean ultimaLuta) {
		if (!ultimaLuta) {
			if (getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Helmet") != null) {
				p.getInventory().setHelmet(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Helmet"))));
			}
			if (getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Armor") != null) {
				p.getInventory().setChestplate(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Armor"))));
			}
			if (getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Legs") != null) {
				p.getInventory().setLeggings(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Legs"))));
			}
			if (getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Boots") != null) {
				p.getInventory().setBoots(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Lutas_Normais.Armadura.Boots"))));
			}
			for (String linha : getConfig().getStringList("Itens_Lutas.Lutas_Normais.Inventario")) {
				p.getInventory().addItem(new ItemStack(ItemStackFormat.getItem(linha)));
			}
			p.updateInventory();
		} else {
			if (getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Helmet") != null) {
				p.getInventory().setHelmet(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Helmet"))));
			}
			if (getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Armor") != null) {
				p.getInventory().setChestplate(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Armor"))));
			}
			if (getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Legs") != null) {
				p.getInventory().setLeggings(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Legs"))));
			}
			if (getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Boots") != null) {
				p.getInventory().setBoots(new ItemStack(
						ItemStackFormat.getItem(getConfig().getString("Itens_Lutas.Ultima_Luta.Armadura.Boots"))));
			}
			for (String linha : getConfig().getStringList("Itens_Lutas.Ultima_Luta.Inventario")) {
				p.getInventory().addItem(new ItemStack(ItemStackFormat.getItem(linha)));
			}
			p.updateInventory();
		}
	}

	public FightListener getListener() {
		return this.listener;
	}

	public Player getLutador1() {
		return lutador1;
	}

	public Player getLutador2() {
		return lutador2;
	}

	public List<Player> getPrimeiraRodada() {
		return rodada;
	}

	public List<Player> getSegundaRodada() {
		return rodadaProxima;
	}

	public BukkitTask taskLuta() {
		return taskLuta;
	}

	public void setLutaOcorrendo(boolean lutaOcorrendo) {
		this.lutaOcorrendo = lutaOcorrendo;
	}

	public List<ClanPlayer> getClans() {
		return clans;
	}
}
