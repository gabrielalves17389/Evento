package me.gabriel.eventos;

import java.io.File;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.gabriel.eventos.api.EventoCancellType;
import me.gabriel.eventos.api.events.PlayerEnterEvent;
import me.gabriel.eventos.api.events.PlayerLeaveEvent;
import me.gabriel.eventos.api.events.PlayerLoseEvent;
import me.gabriel.eventos.api.events.PlayerWinEvent;
import me.gabriel.eventos.api.events.StopEvent;
import me.gabriel.eventos.api.events.TeamWinEvent;
import me.gabriel.eventos.utils.ItemStackFormat;
import me.gabriel.eventos.utils.Title;

public class MainListeners implements Listener {

	@EventHandler
	public void onStopEvent(StopEvent e) {
		for (Player p : e.getEvento().getParticipantes()) {
			if (e.getEvento().isInventoryEmpty()) {
				p.getInventory().setHelmet(null);
				p.getInventory().setChestplate(null);
				p.getInventory().setLeggings(null);
				p.getInventory().setBoots(null);
				p.getInventory().clear();
			}
			p.teleport(e.getEvento().getSaida());
		}
		for (Player p : e.getEvento().getCamarotePlayers()) {
			p.teleport(e.getEvento().getSaida());
		}
		e.getEvento().getParticipantes().clear();
		e.getEvento().getCamarotePlayers().clear();
		e.getEvento().resetEvent();
		if (e.getCancellType() == EventoCancellType.CANCELLED
				|| e.getCancellType() == EventoCancellType.SERVER_STOPED) {
			e.getEvento().cancelEventMethod();
		} else {
			e.getEvento().stopEventMethod();
		}
	}

	@EventHandler
	public void onEventoPlayerOutEvent(PlayerLeaveEvent e) {
		if (e.isAssistindo()) {
			Main.getEventos().getEventosController().getEvento().getCamarotePlayers().remove(e.getPlayer());
			e.getPlayer().teleport(Main.getEventos().getEventosController().getEvento().getSaida());
			return;
		}
		PlayerLoseEvent event = new PlayerLoseEvent(e.getPlayer(),
				Main.getEventos().getEventosController().getEvento());
		Main.getEventos().getServer().getPluginManager().callEvent(event);
	}

	@EventHandler
	public void onEventoPlayerLoseEvent(PlayerLoseEvent e) {
		if (e.getEvento().isInventoryEmpty()) {
			e.getPlayer().getInventory().setHelmet(null);
			e.getPlayer().getInventory().setChestplate(null);
			e.getPlayer().getInventory().setLeggings(null);
			e.getPlayer().getInventory().setBoots(null);
			e.getPlayer().getInventory().clear();
		}
		e.getEvento().getParticipantes().remove(e.getPlayer());
		e.getPlayer().teleport(e.getEvento().getSaida());
	}

	@EventHandler
	public void onTimeWinEvent(TeamWinEvent e) {
		for (Player p : e.getList()) {
			PlayerWinEvent event = new PlayerWinEvent(p, Main.getEventos().getEventosController().getEvento(),
					true);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
		}
		for (String s : e.getEvento().getConfig().getStringList("Mensagens.Vencedor")) {
			Main.getEventos().getServer().broadcastMessage(s.replaceAll("&", "§")
					.replace("$player$", e.getNomeTime()).replace("$EventoName$", e.getEvento().getNome()));
		}
	}

	@EventHandler
	public void onEventoPlayerWinEvent(PlayerWinEvent e) {
		if (e.getEvento().isInventoryEmpty()) {
			e.getPlayer().getInventory().setHelmet(null);
			e.getPlayer().getInventory().setChestplate(null);
			e.getPlayer().getInventory().setLeggings(null);
			e.getPlayer().getInventory().setBoots(null);
			e.getPlayer().getInventory().clear();
		}
		if (e.getEvento().isContarVitoria()) {
			Main.getEventos().getDatabaseManager().addWinPoint(e.getPlayer().getName(), 1);
		}
		if (e.getEvento().getConfig().getStringList("Premios.Itens") != null) {
			for (String linha : e.getEvento().getConfig().getStringList("Premios.Itens")) {
				e.getPlayer().getInventory().addItem(new ItemStack(ItemStackFormat.getItem(linha)));
			}
		}
		if (e.getEvento().getConfig().getStringList("Premios.Comandos") != null) {
			for (String comando : e.getEvento().getConfig().getStringList("Premios.Comandos")) {
				Main.getEventos().getServer().dispatchCommand(
						Main.getEventos().getServer().getConsoleSender(),
						comando.replace("$player$", e.getPlayer().getName()));
			}
		}
		if (Main.getEventos().getEconomy() != null) {
			Main.getEventos().getEconomy().depositPlayer(e.getPlayer(),
					e.getEvento().getConfig().getDouble("Premios.Money")
							* Main.getEventos().getConfig().getInt("Money_Multiplicador"));
		}
		if (!e.isTeamEvent()) {
			for (String s : e.getEvento().getConfig().getStringList("Mensagens.Vencedor")) {
				Main.getEventos().getServer().broadcastMessage(s.replaceAll("&", "§")
						.replace("$player$", e.getPlayer().getName()).replace("$EventoName$", e.getEvento().getNome()));
			}
		}

		e.getEvento().getParticipantes().remove(e.getPlayer());
		e.getPlayer().teleport(e.getEvento().getSaida());
	}
	
	@EventHandler
	public void EntrouNoEvento(PlayerEnterEvent e) {
		final Player p = e.getPlayer();
		Title.EnviarTitleMsg(p, "§7Você entrou no evento", "§a" + e.getEventName());
	}
	
	@EventHandler
	public void SaiuDoEvento(PlayerLeaveEvent e) {
		final Player p = e.getPlayer();
		Title.EnviarTitleMsg(p, "§7Você saiu do evento", "§c" + e.getEventName());
		
	}
	
	@EventHandler
	public void PerdeuEvento(PlayerLoseEvent e) {
		final Player p = e.getPlayer();
		Title.EnviarTitleMsg(p, "§7" + e.getEventName(), "§7Você foi §cELIMINADO");
		
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		if (!e.getPlayer().hasPermission("heventos.admin"))
			return;
		if (!(e.getPlayer().getItemInHand().getType() == Material.IRON_AXE))
			return;
		if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Evento Spleef")) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/spleef.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Chao_1",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "spleef.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 1 do chao do spleef setada!");
				e.setCancelled(true);
			} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/spleef.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Chao_2",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "spleef.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 2 do chao do spleef setada!");
				e.setCancelled(true);
			}
		} else if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Evento MinaMortal")) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/minamortal.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Mina_1",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "minamortal.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 1 da mina setada!");
				e.setCancelled(true);
			} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/minamortal.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Mina_2",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "minamortal.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 2 da mina setada!");
				e.setCancelled(true);
			}
		} else if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("BowSpleef")) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/bowspleef.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Chao_1",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "bowspleef.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 1 do bowspleef setada!");
				e.setCancelled(true);
			} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/bowspleef.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Chao_2",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "bowspleef.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 2 do bowspleef setada!");
				e.setCancelled(true);
			}

		} else if (e.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Frog")) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/frog.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Chao_1",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "frog.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 1 do frog setada!");
				e.setCancelled(true);
			} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
				File fileEvento = new File(
						Main.getEventos().getDataFolder().getAbsolutePath() + "/Eventos/frog.yml");
				YamlConfiguration configEvento = YamlConfiguration.loadConfiguration(fileEvento);
				configEvento.set("Localizacoes.Chao_2",
						e.getClickedBlock().getWorld().getName() + ";" + (int) e.getClickedBlock().getLocation().getX()
								+ ";" + (int) e.getClickedBlock().getLocation().getY() + ";"
								+ (int) e.getClickedBlock().getLocation().getZ());
				try {
					configEvento.save(new File(Main.getEventos().getDataFolder() + File.separator + "Eventos"
							+ File.separator + "frog.yml"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.getPlayer().sendMessage("§4[Evento] §cLocalizacao 2 do frog setada!");
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onSignChangeEvent(SignChangeEvent e) {
		if (e.getPlayer().hasPermission("eventos.admin")) {
			if (e.getLine(0).equalsIgnoreCase("[Evento]")) {
				e.setLine(0, "§9[Evento]");
				e.getPlayer().sendMessage("§4[Evento] §cPlaca criada com sucesso!");
			}
		}
	}

}
