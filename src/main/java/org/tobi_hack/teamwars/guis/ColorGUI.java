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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ColorGUI implements InventoryHolder {

    private final Main plugin;
    private final Player player;
    private final Team team;
    private Inventory inventory;
    private ChatColor selectedColor;

    public ColorGUI(Main plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.team = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());
        this.selectedColor = team != null ? team.getColor() : ChatColor.WHITE;
    }

    public void open() {
        if (team == null) {
            player.sendMessage(ChatColor.RED + "No perteneces a ningún equipo!");
            return;
        }

        if (!team.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Solo el líder puede cambiar el color del equipo!");
            return;
        }

        this.inventory = Bukkit.createInventory(this, 27, "§8Seleccionar Color del Equipo");

        // 16 colores disponibles
        ChatColor[] colors = {
                ChatColor.WHITE, ChatColor.LIGHT_PURPLE, ChatColor.BLUE, ChatColor.YELLOW,
                ChatColor.GREEN, ChatColor.RED, ChatColor.GRAY, ChatColor.DARK_GRAY,
                ChatColor.AQUA, ChatColor.DARK_PURPLE, ChatColor.DARK_BLUE, ChatColor.GOLD,
                ChatColor.DARK_GREEN, ChatColor.DARK_RED, ChatColor.BLACK, ChatColor.DARK_AQUA
        };

        String[] colorNames = {
                "Blanco", "Rosa", "Azul", "Amarillo",
                "Verde", "Rojo", "Gris Claro", "Gris Oscuro",
                "Cian", "Púrpura", "Azul Oscuro", "Naranja",
                "Verde Oscuro", "Rojo Oscuro", "Negro", "Cian Oscuro"
        };

        Material[] colorMaterials = {
                Material.WHITE_WOOL, Material.PINK_WOOL, Material.BLUE_WOOL, Material.YELLOW_WOOL,
                Material.LIME_WOOL, Material.RED_WOOL, Material.LIGHT_GRAY_WOOL, Material.GRAY_WOOL,
                Material.LIGHT_BLUE_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.ORANGE_WOOL,
                Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL, Material.CYAN_WOOL
        };

        for (int i = 0; i < 16 && i < colors.length; i++) {
            ItemStack item = buildColorItem(colorMaterials[i], colors[i], colorNames[i]);
            inventory.setItem(i, item);
        }

        // Ítem de confirmación
        ItemStack confirm = buildItem(Material.LIME_DYE,
                ChatColor.GREEN + "Confirmar Cambio de Color",
                "",
                ChatColor.GRAY + "Color seleccionado: " + selectedColor + getColorName(selectedColor),
                ChatColor.GRAY + "Color actual: " + team.getColor() + getColorName(team.getColor()),
                "",
                ChatColor.YELLOW + "Click para confirmar"
        );
        inventory.setItem(21, confirm);

        // Ítem de cancelar
        ItemStack cancel = buildItem(Material.RED_DYE,
                ChatColor.RED + "Cancelar",
                "",
                ChatColor.GRAY + "No se cambiará el color",
                "",
                ChatColor.YELLOW + "Click para cancelar"
        );
        inventory.setItem(23, cancel);

        // Ítem de información
        ItemStack info = buildItem(Material.BOOK,
                ChatColor.GOLD + "Información",
                "",
                ChatColor.GRAY + "Al cambiar el color del equipo:",
                ChatColor.GRAY + "• Todos los miembros tendrán glowing",
                ChatColor.GRAY + "• Se actualizará el prefijo del equipo",
                ChatColor.GRAY + "• Cambiará el color en el tablist",
                "",
                ChatColor.YELLOW + "Selecciona un color arriba"
        );
        inventory.setItem(22, info);

        // Rellenar slots vacíos
        ItemStack filler = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        player.openInventory(inventory);
    }

    private ItemStack buildColorItem(Material material, ChatColor color, String colorName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        meta.setDisplayName(color + colorName);

        List<String> lore = new ArrayList<>();
        lore.add("");

        if (selectedColor == color) {
            lore.add(ChatColor.GREEN + "✓ Color seleccionado");
            lore.add(ChatColor.GRAY + "Click para confirmar");
        } else if (team.getColor() == color) {
            lore.add(ChatColor.YELLOW + "✓ Color actual del equipo");
            lore.add(ChatColor.GRAY + "Click para seleccionar este color");
        } else {
            lore.add(ChatColor.YELLOW + "Click para seleccionar");
            lore.add(ChatColor.GRAY + "Tu equipo será de color " + color + colorName);
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
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

    public void handleClick(int slot) {
        if (slot < 0 || slot >= inventory.getSize()) return;

        ChatColor[] colors = {
                ChatColor.WHITE, ChatColor.LIGHT_PURPLE, ChatColor.BLUE, ChatColor.YELLOW,
                ChatColor.GREEN, ChatColor.RED, ChatColor.GRAY, ChatColor.DARK_GRAY,
                ChatColor.AQUA, ChatColor.DARK_PURPLE, ChatColor.DARK_BLUE, ChatColor.GOLD,
                ChatColor.DARK_GREEN, ChatColor.DARK_RED, ChatColor.BLACK, ChatColor.DARK_AQUA
        };

        if (slot < 16) {
            // Seleccionar color
            selectedColor = colors[slot];
            open(); // Reabrir para actualizar la GUI

        } else if (slot == 21) {
            // Confirmar
            applyColorChange();

        } else if (slot == 23) {
            // Cancelar
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Cambio de color cancelado.");
        }
    }

    private void applyColorChange() {
        team.setColor(selectedColor);

        // Notificar al líder
        player.sendMessage(ChatColor.GREEN + "¡Color del equipo cambiado a " +
                selectedColor + getColorName(selectedColor) + ChatColor.GREEN + "!");

        // Aplicar glowing a todos los miembros online
        for (UUID memberId : team.getOnlineMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(ChatColor.GREEN + "El color del equipo ha sido cambiado a " +
                        selectedColor + getColorName(selectedColor) + ChatColor.GREEN + "!");
            }
        }

        player.closeInventory();
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
