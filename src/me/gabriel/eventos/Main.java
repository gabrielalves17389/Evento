package me.gabriel.eventos;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.gabriel.eventos.api.EventoBaseAPI;
import me.gabriel.eventos.api.EventoCancellType;
import me.gabriel.eventos.api.events.StopEvent;
import me.gabriel.eventos.databases.Database;
import me.gabriel.eventos.databases.DatabaseType;
import me.gabriel.eventos.hooks.LegendChat;
import me.gabriel.eventos.utils.AutoStartEvents;
import me.gabriel.eventos.utils.ConfigUtil;
import me.gabriel.eventos.utils.EventosController;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;

public class Main extends JavaPlugin {

	private List<EventoBaseAPI> externalEventos = new ArrayList<>();
	private EventosController eventosController;
	private Database databaseManager;
	private Economy economy = null;
	private SimpleClans sc = null;
	private LegendChat lc = null;
	private ConfigUtil configUtil;
	@Override
	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fIniciando o plugina!");
		loadConfigs();
		loadDatabase();
		loadCommands();
		loadListeners();
		loadDependencies();
		loadEventos();
		Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fPlugin Habilitado - (Versao §9" + this.getDescription().getVersion() + "§f)");
	}

	@Override
	public void onDisable() {
		if (eventosController.getEvento() != null) {
			StopEvent event = new StopEvent(Main.getEventos().getEventosController().getEvento(),
					EventoCancellType.SERVER_STOPED);
			Main.getEventos().getServer().getPluginManager().callEvent(event);
			this.eventosController.getEvento().stopEvent();
			eventosController.setEvento(null);
		}
		Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fPlugin Desabilitado - (Versao §9" + this.getDescription().getVersion() + "§f)");
	}

	private void loadConfigs() {
		if (!new File(this.getDataFolder(), "config.yml").exists()) {
			getConfig().options().copyDefaults(true);
	        saveConfig();
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fConfig.yml criada com sucesso!");
		} else {
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fConfig.yml carregada com sucesso!");
		}
		File eventosFile = new File(this.getDataFolder() + File.separator + "Eventos");
		if (!eventosFile.exists()) {
			eventosFile.mkdirs();
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fPasta 'Eventos' criada com sucesso!");
		}
		if (this.getConfig().getBoolean("Ativar_Configs_Exemplos")) {
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "eventoexemplo.yml")
					.exists()) {
				this.saveResource("Eventos" + File.separator + "eventoexemplo.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "batataquente.yml")
					.exists()) {
				this.saveResource("Eventos" + File.separator + "batataquente.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "spleef.yml").exists()) {
				this.saveResource("Eventos" + File.separator + "spleef.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "killer.yml").exists()) {
				this.saveResource("Eventos" + File.separator + "killer.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "minamortal.yml")
					.exists()) {
				this.saveResource("Eventos" + File.separator + "minamortal.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "paintball.yml")
					.exists()) {
				this.saveResource("Eventos" + File.separator + "paintball.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "semaforo.yml")
					.exists()) {
				this.saveResource("Eventos" + File.separator + "semaforo.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "bowspleef.yml")
					.exists()) {
				this.saveResource("Eventos" + File.separator + "bowspleef.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "frog.yml").exists()) {
				this.saveResource("Eventos" + File.separator + "frog.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Eventos" + File.separator + "fight.yml").exists()) {
				this.saveResource("Eventos" + File.separator + "fight.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "Tags.yml").exists()) {
				this.saveResource("Tags.yml", false);
			}
			if (!new File(this.getDataFolder() + File.separator + "GUIA PARA ITENS.yml").exists()) {
				this.saveResource("GUIA PARA ITENS.yml", false);
			}
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fConfigs de exemplos criadas!");
		}
		this.configUtil = new ConfigUtil();
	}
	
	private void loadDependencies() {
		if (!setupSimpleClans()) {
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §cSimpleClans não encontrado!");
		}
		if (!setupEconomy()) {
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §cVault não encontrado!");
		}
		if (!setupLegendChat()) {
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §cLegendChat não encontrado!");
		}
	}

	private void loadDatabase() {
		if (this.getConfig().getBoolean("MySQL.Ativado") == true) {
			databaseManager = new Database(DatabaseType.MYSQL, this.getConfig().getString("MySQL.Usuario"),
					this.getConfig().getString("MySQL.Senha"), this.getConfig().getString("MySQL.Database"),
					this.getConfig().getString("MySQL.Host"));
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fMySQL Habilitado!");
		} else {
			File databaseFile;
			databaseFile = new File(this.getDataFolder() + File.separator + "database.db");
			if (!databaseFile.exists()) {
				try {
					databaseFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fDatabase.db criada com sucesso!");
			}
			databaseManager = new Database(DatabaseType.SQLITE);
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fSQLite Habilitado!");
		}
	}

	private void loadCommands() {
		this.getCommand("evento").setExecutor(new Comandos(this));
	}

	private void loadListeners() {
		getServer().getPluginManager().registerEvents(new MainListeners(), this);
	}

	private void loadEventos() {
		this.eventosController = new EventosController(this);
		this.eventosController.setEvento(null);
		AutoStartEvents.AutoStart();
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fVault encontrado com sucesso!");
		economy = rsp.getProvider();
		return economy != null;
	}

	private boolean setupSimpleClans() {
		Plugin plug = this.getServer().getPluginManager().getPlugin("SimpleClans");
		if (plug != null) {
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fSimpleClans encontrado com sucesso!");
			this.sc = ((SimpleClans) plug);
			return true;
		}
		return false;
	}

	private boolean setupLegendChat() {
		Plugin plug = getServer().getPluginManager().getPlugin("LegendChat");
		if (plug != null) {
			Bukkit.getConsoleSender().sendMessage("§9[Eventos] §fLegendChat encontrado com sucesso!");
			this.lc = new LegendChat();
			this.getServer().getPluginManager().registerEvents(this.lc, this);
			return true;
		}
		return false;
	}

	public Economy getEconomy() {
		return this.economy;
	}

	public EventosController getEventosController() {
		return this.eventosController;
	}

	public SimpleClans getSc() {
		return this.sc;
	}

	public LegendChat getLc() {
		return lc;
	}
	
	public ConfigUtil getConfigUtil() {
		return this.configUtil;
	}
	
	public Database getDatabaseManager() {
		return databaseManager;
	}

	public List<EventoBaseAPI> getExternalEventos() {
		return externalEventos;
	}
	
	public static Main getEventos() {
		return (Main) Bukkit.getServer().getPluginManager().getPlugin("Eventos");
	}
}
