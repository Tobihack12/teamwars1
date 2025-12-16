package org.tobi_hack.teamwars;

import org.tobi_hack.teamwars.commands.CommandManager;
import org.tobi_hack.teamwars.listeners.PlayerListener;
import org.tobi_hack.teamwars.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private TeamManager teamManager;
    private BattleManager battleManager;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Inicializar managers
        this.configManager = new ConfigManager();
        this.databaseManager = new DatabaseManager(this);
        this.teamManager = new TeamManager(this);
        this.battleManager = new BattleManager(this);

// Registrar comandos
        new CommandManager(this);

// Registrar listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Cargar datos
        databaseManager.loadAllTeams();

        // Registrar PlaceholderAPI si está presente
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new org.tobi_hack.teamwars.placeholders.TeamWarsExpansion(this).register();
            getLogger().info("PlaceholderAPI conectado correctamente!");
        }

        getLogger().info("TeamWars v" + getDescription().getVersion() + " activado!");
    }

    @Override
    public void onDisable() {
        // Guardar datos
        databaseManager.saveAllTeams();
        getLogger().info("TeamWars desactivado!");
    }

    public static Main getInstance() {
        return instance;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public BattleManager getBattleManager() {
        return battleManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}