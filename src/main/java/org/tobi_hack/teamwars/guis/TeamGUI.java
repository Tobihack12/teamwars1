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

import java.text.SimpleDateFormat;
import java.util.*;

public class TeamGUI implements InventoryHolder {

    private final Main plugin;
    private final Player player;
    private final Team playerTeam;
    private Inventory inventory;

    public TeamGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
    }

    public void open() {
        if (playerTeam == null) {
            openNoTeamGUI();
        } else {
            openTeamMainGUI();
        }
    }

    private void openNoTeamGUI() {
        this.inventory = Bukkit.createInventory(this, 27,
                ChatColor.DARK_GRAY + "TeamWars - Sin Equipo");

        ItemStack createTeam = buildItem(Material.WRITABLE_BOOK,
                ChatColor.GREEN + "Crear Nuevo Equipo",
                "",
                ChatColor.GRAY + "Crea tu propio equipo",
                ChatColor.GRAY + "y conviértete en líder",
                "",
                ChatColor.YELLOW + "Requisitos:",
                ChatColor.GRAY + "• No pertenecer a otro equipo",
                ChatColor.GRAY + "• Nombre único (3-16 caracteres)",
                "",
                ChatColor.YELLOW + "Comando: " + ChatColor.WHITE + "/team crear <nombre>"
        );
        inventory.setItem(11, createTeam);

        ItemStack joinTeam = buildItem(Material.PAPER,
                ChatColor.BLUE + "Unirse a un Equipo",
                "",
                ChatColor.GRAY + "Busca equipos disponibles",
                ChatColor.GRAY + "y solicita unirte",
                "",
                ChatColor.YELLOW + "Equipos disponibles: " +
                        ChatColor.WHITE + plugin.getTeamManager().getTotalTeams(),
                ChatColor.YELLOW + "Jugadores en equipos: " +
                        ChatColor.WHITE + plugin.getTeamManager().getTotalPlayers(),
                "",
                ChatColor.YELLOW + "Comando: " + ChatColor.WHITE + "/team unir <nombre>"
        );
        inventory.setItem(13, joinTeam);

        ItemStack listTeams = buildItem(Material.COMPASS,
                ChatColor.YELLOW + "Ver Equipos",
                "",
                ChatColor.GRAY + "Explora todos los equipos",
                ChatColor.GRAY + "y sus estadísticas",
                "",
                ChatColor.YELLOW + "Click para ver la lista",
                ChatColor.YELLOW + "o usa " + ChatColor.WHITE + "/team list"
        );
        inventory.setItem(15, listTeams);

        ItemStack info = buildItem(Material.BOOK,
                ChatColor.GOLD + "Información de TeamWars",
                "",
                ChatColor.GRAY + "Sistema de batallas por equipos",
                "",
                ChatColor.YELLOW + "Características:",
                ChatColor.GRAY + "• Máximo 10 jugadores por equipo",
                ChatColor.GRAY + "• Líder con efectos especiales",
                ChatColor.GRAY + "• Sistema de vidas limitadas",
                ChatColor.GRAY + "• Respawn junto al líder",
                ChatColor.GRAY + "• Estadísticas y leaderboards",
                "",
                ChatColor.YELLOW + "Usa " + ChatColor.WHITE + "/team ayuda" +
                        ChatColor.YELLOW + " para más comandos"
        );
        inventory.setItem(26, info);

        ItemStack border = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (i == 11 || i == 13 || i == 15 || i == 26) continue;
            inventory.setItem(i, border);
        }

        player.openInventory(inventory);
    }

    private void openTeamMainGUI() {
        this.inventory = Bukkit.createInventory(this, 54,
                ChatColor.DARK_GRAY + "TeamWars - " + playerTeam.getDisplayName());

        // FILA 1: INFORMACIÓN DEL EQUIPO
        ItemStack leaderHead = createPlayerHead(playerTeam.getLeader(),
                playerTeam.getColor() + "★ Líder del Equipo");

        if (leaderHead != null) {
            SkullMeta leaderMeta = (SkullMeta) leaderHead.getItemMeta();
            if (leaderMeta != null) {
                String leaderName = Bukkit.getOfflinePlayer(playerTeam.getLeader()).getName();
                boolean leaderOnline = Bukkit.getPlayer(playerTeam.getLeader()) != null &&
                        Bukkit.getPlayer(playerTeam.getLeader()).isOnline();

                List<String> leaderLore = new ArrayList<>();
                leaderLore.add("");
                leaderLore.add(ChatColor.GRAY + "Nombre: " + ChatColor.WHITE + leaderName);
                leaderLore.add(ChatColor.GRAY + "Estado: " +
                        (leaderOnline ? ChatColor.GREEN + "En línea" : ChatColor.RED + "Desconectado"));
                leaderLore.add("");

                if (playerTeam.isLeader(player.getUniqueId())) {
                    leaderLore.add(ChatColor.YELLOW + "¡Eres el líder del equipo!");
                    leaderLore.add(ChatColor.GRAY + "Puedes gestionar miembros,");
                    leaderLore.add(ChatColor.GRAY + "cambiar color y eliminar equipo.");
                } else {
                    leaderLore.add(ChatColor.YELLOW + "Tu rango: " +
                            playerTeam.getRank(player.getUniqueId()).getColor() +
                            playerTeam.getRank(player.getUniqueId()).getDisplayName());
                }

                leaderMeta.setLore(leaderLore);
                leaderHead.setItemMeta(leaderMeta);
            }
            inventory.setItem(4, leaderHead);
        }

        ItemStack stats = buildItem(Material.DIAMOND_SWORD,
                ChatColor.GOLD + "Estadísticas del Equipo",
                "",
                ChatColor.GRAY + "Kills: " + ChatColor.WHITE + playerTeam.getKills(),
                ChatColor.GRAY + "Muertes: " + ChatColor.WHITE + playerTeam.getDeaths(),
                ChatColor.GRAY + "K/D: " + ChatColor.WHITE + String.format("%.2f", playerTeam.getKD()),
                ChatColor.GRAY + "Victorias: " + ChatColor.WHITE +
                        playerTeam.getWins() + "/" + playerTeam.getBattles(),
                ChatColor.GRAY + "Win Rate: " + ChatColor.WHITE +
                        (playerTeam.getBattles() > 0 ?
                                String.format("%.1f%%", (double) playerTeam.getWins() / playerTeam.getBattles() * 100) :
                                "0%"),
                "",
                ChatColor.GRAY + "Miembros: " + ChatColor.WHITE +
                        playerTeam.getMemberCount() + "/10",
                ChatColor.GRAY + "Online: " + ChatColor.WHITE +
                        playerTeam.getOnlineMembers().size(),
                "",
                ChatColor.YELLOW + "Estado: " +
                        (playerTeam.isInBattle() ?
                                ChatColor.RED + "⚔ En Batalla" :
                                ChatColor.GREEN + "✓ Disponible")
        );
        inventory.setItem(6, stats);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String creationDate = sdf.format(new Date(playerTeam.getCreatedAt()));

        ItemStack info = buildItem(Material.BOOK,
                playerTeam.getColor() + playerTeam.getName(),
                "",
                ChatColor.GRAY + "Creado: " + ChatColor.WHITE + creationDate,
                ChatColor.GRAY + "Antigüedad: " + ChatColor.WHITE + playerTeam.getFormattedAge(),
                ChatColor.GRAY + "Color: " + playerTeam.getColor() + getColorName(playerTeam.getColor()),
                ChatColor.GRAY + "Prefijo: " + playerTeam.getPrefix(),
                ChatColor.GRAY + "ID: " + ChatColor.WHITE +
                        playerTeam.getTeamId().toString().substring(0, 8) + "..."
        );
        inventory.setItem(2, info);

        // FILA 2-4: MIEMBROS
        int memberSlot = 9;
        List<UUID> allMembers = new ArrayList<>(playerTeam.getMembers());

        allMembers.sort((id1, id2) -> {
            if (id1.equals(playerTeam.getLeader())) return -1;
            if (id2.equals(playerTeam.getLeader())) return 1;

            Team.TeamRank rank1 = playerTeam.getRank(id1);
            Team.TeamRank rank2 = playerTeam.getRank(id2);
            if (rank1.getPower() != rank2.getPower()) {
                return rank2.getPower() - rank1.getPower();
            }

            String name1 = Bukkit.getOfflinePlayer(id1).getName();
            String name2 = Bukkit.getOfflinePlayer(id2).getName();
            return name1.compareToIgnoreCase(name2);
        });

        for (UUID memberId : allMembers) {
            if (memberSlot >= 44) break;

            ItemStack memberHead = createMemberHead(memberId);
            if (memberHead != null) {
                inventory.setItem(memberSlot, memberHead);
            }
            memberSlot++;
        }

        ItemStack emptySlot = buildItem(Material.GRAY_STAINED_GLASS_PANE,
                ChatColor.GRAY + "Vacío",
                "",
                ChatColor.GRAY + "Espacio disponible para",
                ChatColor.GRAY + "nuevos miembros del equipo"
        );

        while (memberSlot < 44) {
            inventory.setItem(memberSlot, emptySlot);
            memberSlot++;
        }

        // FILA 5: ACCIONES
        setupActionButtons();

        player.openInventory(inventory);
    }

    private ItemStack createPlayerHead(UUID playerId, String displayName) {
        try {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerId));
                meta.setDisplayName(displayName);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
                head.setItemMeta(meta);
            }
            return head;
        } catch (Exception e) {
            return null;
        }
    }

    private ItemStack createMemberHead(UUID memberId) {
        try {
            ItemStack memberHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta memberMeta = (SkullMeta) memberHead.getItemMeta();

            if (memberMeta == null) return null;

            memberMeta.setOwningPlayer(Bukkit.getOfflinePlayer(memberId));

            String memberName = Bukkit.getOfflinePlayer(memberId).getName();
            boolean isOnline = Bukkit.getPlayer(memberId) != null &&
                    Bukkit.getPlayer(memberId).isOnline();
            Team.TeamRank rank = playerTeam.getRank(memberId);
            boolean isLeader = playerTeam.isLeader(memberId);

            String displayName = rank.getColor() + memberName;
            if (isLeader) {
                displayName = ChatColor.GOLD + "★ " + displayName;
            }
            memberMeta.setDisplayName(displayName);

            List<String> memberLore = new ArrayList<>();
            memberLore.add("");
            memberLore.add(ChatColor.GRAY + "Rango: " + rank.getColor() + rank.getDisplayName());
            memberLore.add(ChatColor.GRAY + "Estado: " +
                    (isOnline ? ChatColor.GREEN + "En línea" : ChatColor.RED + "Desconectado"));

            if (isLeader) {
                memberLore.add(ChatColor.GRAY + "Es líder: " + ChatColor.GOLD + "Sí");
            }

            if (memberId.equals(player.getUniqueId())) {
                memberLore.add("");
                memberLore.add(ChatColor.YELLOW + "¡Este eres tú!");
            }

            if (playerTeam.canKick(player.getUniqueId(), memberId) &&
                    !memberId.equals(player.getUniqueId())) {
                memberLore.add("");
                memberLore.add(ChatColor.YELLOW + "Acciones disponibles:");
                memberLore.add(ChatColor.GRAY + "Click izquierdo: Ver información");
                memberLore.add(ChatColor.GRAY + "Click derecho: Expulsar del equipo");

                if (!isLeader && playerTeam.isLeader(player.getUniqueId())) {
                    memberLore.add(ChatColor.GRAY + "Shift + Click: Cambiar rango");
                }
            }

            memberMeta.setLore(memberLore);
            memberMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
            memberHead.setItemMeta(memberMeta);
            return memberHead;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupActionButtons() {
        if (playerTeam.isLeader(player.getUniqueId())) {
            ItemStack changeColor = buildItem(Material.PAINTING,
                    ChatColor.LIGHT_PURPLE + "Cambiar Color del Equipo",
                    "",
                    ChatColor.GRAY + "Cambia el color principal",
                    ChatColor.GRAY + "de tu equipo",
                    "",
                    ChatColor.GRAY + "Color actual: " + playerTeam.getColor() +
                            getColorName(playerTeam.getColor()),
                    "",
                    ChatColor.YELLOW + "Click para seleccionar",
                    ChatColor.YELLOW + "nuevo color"
            );
            inventory.setItem(45, changeColor);
        } else {
            ItemStack colorInfo = buildItem(Material.PAINTING,
                    ChatColor.GRAY + "Color del Equipo",
                    "",
                    ChatColor.GRAY + "Color actual: " + playerTeam.getColor() +
                            getColorName(playerTeam.getColor()),
                    "",
                    ChatColor.GRAY + "Solo el líder puede",
                    ChatColor.GRAY + "cambiar el color"
            );
            inventory.setItem(45, colorInfo);
        }

        if (playerTeam.isLeader(player.getUniqueId()) ||
                playerTeam.getRank(player.getUniqueId()).getPower() >= 3) {
            ItemStack invite = buildItem(Material.WRITABLE_BOOK,
                    ChatColor.GREEN + "Invitar Jugadores",
                    "",
                    ChatColor.GRAY + "Invita a otros jugadores",
                    ChatColor.GRAY + "a unirse a tu equipo",
                    "",
                    ChatColor.YELLOW + "Espacios disponibles: " +
                            ChatColor.WHITE + (10 - playerTeam.getMemberCount()),
                    "",
                    ChatColor.YELLOW + "Click para ver jugadores",
                    ChatColor.YELLOW + "disponibles para invitar"
            );
            inventory.setItem(47, invite);
        }

        ItemStack teamChat = buildItem(Material.OAK_SIGN,
                ChatColor.BLUE + "Chat del Equipo",
                "",
                ChatColor.GRAY + "Comunícate con los",
                ChatColor.GRAY + "miembros de tu equipo",
                "",
                ChatColor.YELLOW + "Usa " + ChatColor.WHITE + "/team chat <mensaje>",
                ChatColor.YELLOW + "o " + ChatColor.WHITE + "/tc <mensaje>",
                "",
                ChatColor.YELLOW + "Click para enviar un",
                ChatColor.YELLOW + "mensaje rápido"
        );
        inventory.setItem(49, teamChat);

        if (!playerTeam.isLeader(player.getUniqueId())) {
            ItemStack leave = buildItem(Material.BARRIER,
                    ChatColor.RED + "Abandonar Equipo",
                    "",
                    ChatColor.RED + "¡Advertencia!",
                    ChatColor.RED + "Esta acción no se puede deshacer",
                    "",
                    ChatColor.GRAY + "Abandonarás el equipo",
                    ChatColor.GRAY + "y perderás acceso a las",
                    ChatColor.GRAY + "estadísticas del equipo",
                    "",
                    ChatColor.YELLOW + "Shift + Click para confirmar"
            );
            inventory.setItem(51, leave);
        } else {
            ItemStack delete = buildItem(Material.TNT,
                    ChatColor.DARK_RED + "Eliminar Equipo",
                    "",
                    ChatColor.DARK_RED + "¡PELIGRO!",
                    ChatColor.DARK_RED + "Esta acción NO se puede deshacer",
                    "",
                    ChatColor.GRAY + "Eliminarás el equipo",
                    ChatColor.GRAY + "y todas sus estadísticas",
                    ChatColor.GRAY + "Se notificará a todos los miembros",
                    "",
                    ChatColor.YELLOW + "Shift + Click para confirmar"
            );
            inventory.setItem(51, delete);
        }

        if (!playerTeam.getAllies().isEmpty()) {
            List<String> alliesLore = new ArrayList<>();
            alliesLore.add("");
            alliesLore.add(ChatColor.GRAY + "Equipos aliados: " +
                    ChatColor.WHITE + playerTeam.getAllies().size());

            int allyCount = 0;
            for (UUID allyId : playerTeam.getAllies()) {
                if (allyCount >= 5) {
                    alliesLore.add(ChatColor.GRAY + "... y " +
                            (playerTeam.getAllies().size() - 5) + " más");
                    break;
                }
                Team ally = plugin.getTeamManager().getTeamById(allyId);
                if (ally != null) {
                    alliesLore.add(ChatColor.GRAY + "• " + ally.getDisplayName());
                    allyCount++;
                }
            }

            alliesLore.add("");
            alliesLore.add(ChatColor.YELLOW + "Click para gestionar alianzas");

            ItemStack allies = buildItem(Material.GOLDEN_APPLE,
                    ChatColor.GOLD + "Aliados del Equipo",
                    alliesLore.toArray(new String[0])
            );
            inventory.setItem(53, allies);
        } else {
            ItemStack noAllies = buildItem(Material.APPLE,
                    ChatColor.GRAY + "Sin Aliados",
                    "",
                    ChatColor.GRAY + "Tu equipo no tiene",
                    ChatColor.GRAY + "alianzas activas",
                    "",
                    ChatColor.YELLOW + "Click para buscar aliados"
            );
            inventory.setItem(53, noAllies);
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

    public void handleClick(int slot, boolean isRightClick, boolean isShiftClick) {
        if (slot < 0) return;

        if (playerTeam == null) {
            handleNoTeamClick(slot);
        } else {
            handleTeamClick(slot, isRightClick, isShiftClick);
        }
    }

    private void handleNoTeamClick(int slot) {
        switch (slot) {
            case 11:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Para crear un equipo, usa:");
                player.sendMessage(ChatColor.WHITE + "/team crear <nombre-del-equipo>");
                player.sendMessage(ChatColor.GRAY + "Ejemplo: /team crear LosDragones");
                break;

            case 13:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Para unirte a un equipo, usa:");
                player.sendMessage(ChatColor.WHITE + "/team unir <nombre-del-equipo>");
                player.sendMessage(ChatColor.GRAY + "Usa " + ChatColor.WHITE + "/team list" +
                        ChatColor.GRAY + " para ver equipos disponibles");
                break;

            case 15:
                player.closeInventory();
                plugin.getTeamManager().showTeamInfo(player);
                break;
        }
    }

    private void handleTeamClick(int slot, boolean isRightClick, boolean isShiftClick) {
        if (slot >= 9 && slot < 44) {
            int memberIndex = slot - 9;
            List<UUID> allMembers = new ArrayList<>(playerTeam.getMembers());

            if (memberIndex < allMembers.size()) {
                UUID memberId = allMembers.get(memberIndex);
                handleMemberClick(memberId, isRightClick, isShiftClick);
            }
            return;
        }

        switch (slot) {
            case 45:
                if (playerTeam.isLeader(player.getUniqueId())) {
                    player.closeInventory();
                    new ColorGUI(plugin, player).open();
                } else {
                    player.sendMessage(ChatColor.RED + "Solo el líder puede cambiar el color del equipo.");
                }
                break;

            case 47:
                if (playerTeam.isLeader(player.getUniqueId()) ||
                        playerTeam.getRank(player.getUniqueId()).getPower() >= 3) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Para invitar jugadores:");
                    player.sendMessage(ChatColor.WHITE + "/team invite <jugador>");
                } else {
                    player.sendMessage(ChatColor.RED + "No tienes permiso para invitar jugadores.");
                }
                break;

            case 49:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Para usar el chat del equipo:");
                player.sendMessage(ChatColor.WHITE + "/team chat <mensaje>");
                player.sendMessage(ChatColor.WHITE + "o /tc <mensaje> (abreviado)");
                break;

            case 51:
                if (isShiftClick) {
                    player.closeInventory();
                    if (playerTeam.isLeader(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "¿Estás seguro de eliminar el equipo?");
                        player.sendMessage(ChatColor.YELLOW + "Usa " + ChatColor.WHITE +
                                "/team eliminar confirmar" + ChatColor.YELLOW + " para confirmar.");
                    } else {
                        plugin.getTeamManager().leaveTeam(player);
                    }
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Shift + Click para confirmar.");
                }
                break;

            case 53:
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Para gestionar alianzas:");
                player.sendMessage(ChatColor.WHITE + "/team ally invite <equipo>");
                player.sendMessage(ChatColor.WHITE + "/team ally list");
                break;
        }
    }

    private void handleMemberClick(UUID memberId, boolean isRightClick, boolean isShiftClick) {
        String memberName = Bukkit.getOfflinePlayer(memberId).getName();

        if (memberId.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Ese eres tú! No puedes realizar acciones sobre ti mismo.");
            return;
        }

        if (!playerTeam.canKick(player.getUniqueId(), memberId)) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para realizar acciones sobre este miembro.");
            return;
        }

        if (isRightClick) {
            Player target = Bukkit.getPlayer(memberId);
            if (target != null) {
                plugin.getTeamManager().kickMember(player, target);
                player.closeInventory();
            }
        } else if (isShiftClick && playerTeam.isLeader(player.getUniqueId())) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Para cambiar el rango de " + memberName + ":");
            player.sendMessage(ChatColor.WHITE + "/team setrank " + memberName + " <rango>");
        } else {
            showMemberInfo(memberId, memberName);
        }
    }

    private void showMemberInfo(UUID memberId, String memberName) {
        Team.TeamRank rank = playerTeam.getRank(memberId);
        boolean isOnline = Bukkit.getPlayer(memberId) != null &&
                Bukkit.getPlayer(memberId).isOnline();

        player.sendMessage(ChatColor.GOLD + "════════════════════════════");
        player.sendMessage(ChatColor.YELLOW + "Información de " + memberName);
        player.sendMessage(ChatColor.GRAY + "Rango: " + rank.getColor() + rank.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "Estado: " +
                (isOnline ? ChatColor.GREEN + "En línea" : ChatColor.RED + "Desconectado"));

        if (playerTeam.isLeader(memberId)) {
            player.sendMessage(ChatColor.GOLD + "Es líder del equipo");
        }

        player.sendMessage(ChatColor.GOLD + "════════════════════════════");
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
