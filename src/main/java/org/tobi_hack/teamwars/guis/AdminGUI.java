package org.tobi_hack.teamwars.guis;

import org.tobi_hack.teamwars.Main;
import org.tobi_hack.teamwars.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class AdminGUI implements InventoryHolder {

    private final Main plugin;
    private final Player admin;
    private int currentPage = 0;
    private final int pageSize = 45;
    private Inventory inventory;

    public AdminGUI(Main plugin, Player admin) {
        this.plugin = plugin;
        this.admin = admin;
    }

    public void open(int page) {
        this.currentPage = page;
        List<Team> allTeams = new ArrayList<>(plugin.getTeamManager().getAllTeams());
        int totalPages = (int) Math.ceil((double) allTeams.size() / pageSize);

        this.inventory = Bukkit.createInventory(this, 54,
                "§8Panel Admin - Página " + (page + 1) + "/" + Math.max(1, totalPages));

        // Agregar equipos de esta página
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allTeams.size());

        for (int i = startIndex; i < endIndex; i++) {
            Team team = allTeams.get(i);
            ItemStack teamItem = createTeamItem(team);
            if (teamItem != null) {
                inventory.setItem(i - startIndex, teamItem);
            }
        }

        // Botones de navegación
        if (page > 0) {
            ItemStack prevPage = buildItem(Material.ARROW,
                    ChatColor.YELLOW + "← Página anterior",
                    ChatColor.GRAY + "Click para ir a la página " + page
            );
            inventory.setItem(45, prevPage);
        }

        if (page < totalPages - 1) {
            ItemStack nextPage = buildItem(Material.ARROW,
                    ChatColor.YELLOW + "Página siguiente →",
                    ChatColor.GRAY + "Click para ir a la página " + (page + 2)
            );
            inventory.setItem(53, nextPage);
        }

        // Estadísticas generales
        ItemStack stats = buildItem(Material.PAPER,
                ChatColor.GOLD + "Estadísticas del Servidor",
                "",
                ChatColor.GRAY + "Equipos totales: " + ChatColor.WHITE + allTeams.size(),
                ChatColor.GRAY + "Jugadores en equipos: " + ChatColor.WHITE +
                        plugin.getTeamManager().getTotalPlayers(),
                ChatColor.GRAY + "Batallas activas: " + ChatColor.WHITE +
                        plugin.getBattleManager().getActiveBattlesCount(),
                ChatColor.GRAY + "Kills totales: " + ChatColor.WHITE +
                        plugin.getBattleManager().getTotalKills(),
                "",
                ChatColor.YELLOW + "Click para recargar"
        );
        inventory.setItem(49, stats);

        // Botón para forzar batalla
        ItemStack forceBattle = buildItem(Material.DIAMOND_SWORD,
                ChatColor.RED + "Forzar Batalla",
                "",
                ChatColor.GRAY + "Inicia una batalla con todos",
                ChatColor.GRAY + "los equipos disponibles",
                "",
                ChatColor.YELLOW + "Click para iniciar"
        );
        inventory.setItem(47, forceBattle);

        // Botón para recargar plugin
        ItemStack reload = buildItem(Material.REDSTONE_TORCH,
                ChatColor.GREEN + "Recargar Plugin",
                "",
                ChatColor.GRAY + "Recarga la configuración",
                ChatColor.GRAY + "y los datos del plugin",
                "",
                ChatColor.YELLOW + "Click para recargar"
        );
        inventory.setItem(51, reload);

        // Rellenar slots vacíos
        fillEmptySlots();

        admin.openInventory(inventory);
    }

    private ItemStack createTeamItem(Team team) {
        try {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();

            if (meta == null) return null;

            meta.setOwningPlayer(Bukkit.getOfflinePlayer(team.getLeader()));
            meta.setDisplayName(team.getColor() + team.getName());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Líder: " + ChatColor.WHITE +
                    Bukkit.getOfflinePlayer(team.getLeader()).getName());
            lore.add(ChatColor.GRAY + "Miembros: " + ChatColor.WHITE +
                    team.getMemberCount() + "/10");
            lore.add(ChatColor.GRAY + "Kills: " + ChatColor.WHITE + team.getKills());
            lore.add(ChatColor.GRAY + "K/D: " + ChatColor.WHITE +
                    String.format("%.2f", team.getKD()));
            lore.add(ChatColor.GRAY + "Victorias: " + ChatColor.WHITE +
                    team.getWins() + "/" + team.getBattles());
            lore.add(ChatColor.GRAY + "En batalla: " +
                    (team.isInBattle() ? ChatColor.GREEN + "Sí" : ChatColor.RED + "No"));
            lore.add(ChatColor.GRAY + "Vidas: " + ChatColor.WHITE +
                    (team.getLives() > 0 ? team.getLives() : "∞"));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click izquierdo: Ver detalles");
            lore.add(ChatColor.YELLOW + "Click derecho: Editar equipo");
            lore.add(ChatColor.YELLOW + "Shift + Click: Eliminar equipo");

            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    private ItemStack buildItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(name);

        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    private void fillEmptySlots() {
        ItemStack filler = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public void handleClick(int slot, boolean isRightClick, boolean isShiftClick) {
        if (slot < 0 || slot >= inventory.getSize()) return;

        if (slot >= 0 && slot < 45) {
            // Click en un equipo
            int teamIndex = currentPage * pageSize + slot;
            List<Team> allTeams = new ArrayList<>(plugin.getTeamManager().getAllTeams());

            if (teamIndex < allTeams.size()) {
                Team clickedTeam = allTeams.get(teamIndex);
                if (isShiftClick) {
                    // Eliminar equipo
                    plugin.getTeamManager().deleteTeam(clickedTeam.getTeamId());
                    admin.sendMessage(ChatColor.RED + "Equipo eliminado: " + clickedTeam.getName());
                    open(currentPage);
                } else if (isRightClick) {
                    // Editar equipo
                    openTeamDetails(clickedTeam);
                } else {
                    // Ver detalles
                    openTeamDetails(clickedTeam);
                }
            }
        } else {
            // Click en botones de navegación
            switch (slot) {
                case 45: // Página anterior
                    if (currentPage > 0) {
                        open(currentPage - 1);
                    }
                    break;

                case 53: // Página siguiente
                    List<Team> allTeams = new ArrayList<>(plugin.getTeamManager().getAllTeams());
                    int totalPages = (int) Math.ceil((double) allTeams.size() / pageSize);
                    if (currentPage < totalPages - 1) {
                        open(currentPage + 1);
                    }
                    break;

                case 49: // Estadísticas
                    plugin.reloadConfig();
                    admin.sendMessage(ChatColor.GREEN + "Configuración recargada!");
                    open(currentPage);
                    break;

                case 47: // Forzar batalla
                    plugin.getBattleManager().forceStartBattle();
                    admin.closeInventory();
                    admin.sendMessage(ChatColor.GREEN + "¡Batalla forzada iniciada!");
                    break;

                case 51: // Recargar plugin
                    plugin.onDisable();
                    plugin.onEnable();
                    admin.sendMessage(ChatColor.GREEN + "Plugin recargado completamente!");
                    open(currentPage);
                    break;
            }
        }
    }

    private void openTeamDetails(Team team) {
        Inventory details = Bukkit.createInventory(this, 54,
                "§8Detalles: " + team.getDisplayName());

        // Información básica
        ItemStack info = buildItem(Material.BOOK,
                ChatColor.GOLD + "Información del Equipo",
                "",
                ChatColor.GRAY + "Nombre: " + team.getColor() + team.getName(),
                ChatColor.GRAY + "Color: " + team.getColor() + getColorName(team.getColor()),
                ChatColor.GRAY + "Prefijo: " + team.getPrefix(),
                ChatColor.GRAY + "ID: " + ChatColor.WHITE + team.getTeamId().toString(),
                ChatColor.GRAY + "Creado: " + ChatColor.WHITE +
                        new Date(team.getCreatedAt()).toString(),
                "",
                ChatColor.YELLOW + "Click para cambiar nombre"
        );
        details.setItem(4, info);

        // Miembros del equipo
        int memberSlot = 9;
        for (UUID memberId : team.getMembers()) {
            if (memberSlot >= 44) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();

            if (headMeta != null) {
                headMeta.setOwningPlayer(Bukkit.getOfflinePlayer(memberId));

                Team.TeamRank rank = team.getRank(memberId);
                headMeta.setDisplayName(rank.getColor() + Bukkit.getOfflinePlayer(memberId).getName());

                List<String> memberLore = new ArrayList<>();
                memberLore.add("");
                memberLore.add(ChatColor.GRAY + "Rango: " + rank.getColor() + rank.getDisplayName());
                memberLore.add(ChatColor.GRAY + "Es líder: " +
                        (team.isLeader(memberId) ? ChatColor.GREEN + "Sí" : ChatColor.RED + "No"));
                memberLore.add("");
                memberLore.add(ChatColor.YELLOW + "Click para cambiar rango");
                memberLore.add(ChatColor.YELLOW + "Shift + Click para expulsar");

                headMeta.setLore(memberLore);
                headMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
                head.setItemMeta(headMeta);
            }

            details.setItem(memberSlot, head);
            memberSlot++;
        }

        // Rellenar slots vacíos
        ItemStack filler = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < details.getSize(); i++) {
            if (details.getItem(i) == null) {
                details.setItem(i, filler);
            }
        }

        // Estadísticas
        ItemStack stats = buildItem(Material.DIAMOND_SWORD,
                ChatColor.RED + "Estadísticas de Batalla",
                "",
                ChatColor.GRAY + "Kills: " + ChatColor.WHITE + team.getKills(),
                ChatColor.GRAY + "Muertes: " + ChatColor.WHITE + team.getDeaths(),
                ChatColor.GRAY + "K/D: " + ChatColor.WHITE + String.format("%.2f", team.getKD()),
                ChatColor.GRAY + "Victorias: " + ChatColor.WHITE + team.getWins(),
                ChatColor.GRAY + "Batallas: " + ChatColor.WHITE + team.getBattles(),
                ChatColor.GRAY + "Win Rate: " + ChatColor.WHITE +
                        (team.getBattles() > 0 ? String.format("%.1f%%",
                                (double) team.getWins() / team.getBattles() * 100) : "0%"),
                ChatColor.GRAY + "Vidas actuales: " + ChatColor.WHITE +
                        (team.getLives() > 0 ? team.getLives() : "∞")
        );
        details.setItem(49, stats);

        // Botón para eliminar equipo
        ItemStack delete = buildItem(Material.BARRIER,
                ChatColor.DARK_RED + "Eliminar Equipo",
                "",
                ChatColor.RED + "¡ADVERTENCIA!",
                ChatColor.RED + "Esta acción no se puede deshacer.",
                ChatColor.RED + "Se eliminarán todos los datos del equipo.",
                "",
                ChatColor.YELLOW + "Shift + Click para confirmar"
        );
        details.setItem(53, delete);

        admin.openInventory(details);
    }

    private String getColorName(ChatColor color) {
        return switch (color) {
            case BLACK -> "Negro";
            case DARK_BLUE -> "Azul Oscuro";
            case DARK_GREEN -> "Verde Oscuro";
            case DARK_AQUA -> "Aqua Oscuro";
            case DARK_RED -> "Rojo Oscuro";
            case DARK_PURPLE -> "Púrpura Oscuro";
            case GOLD -> "Oro";
            case GRAY -> "Gris";
            case DARK_GRAY -> "Gris Oscuro";
            case BLUE -> "Azul";
            case GREEN -> "Verde";
            case AQUA -> "Aqua";
            case RED -> "Rojo";
            case LIGHT_PURPLE -> "Rosa";
            case YELLOW -> "Amarillo";
            case WHITE -> "Blanco";
            default -> "Desconocido";
        };
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
