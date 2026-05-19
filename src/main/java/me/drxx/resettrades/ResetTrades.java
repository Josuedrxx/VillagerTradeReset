package me.drxx.resettrades;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResetTrades extends JavaPlugin implements Listener {

    private String lang;
    private boolean enabled;
    private boolean isFolia;
    private String platformMode;

    @Override
    public void onEnable() {
        determinePlatform();
        
        loadPluginConfig();
        getCommand("resettrades").setExecutor(this);
        getCommand("vtr").setExecutor(this);
        getCommand("vtr").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        
        String cyan = "\u001B[36m";
        String green = "\u001B[32m";
        String yellow = "\u001B[33m";
        String magenta = "\u001B[35m";
        String reset = "\u001B[0m";

        String coloredMode = green + platformMode;
        if (platformMode.equals("Folia")) coloredMode = yellow + "Folia (Multithread)";
        else if (platformMode.equals("Purpur")) coloredMode = magenta + "Purpur";

        getLogger().info(cyan + "========================================" + reset);
        getLogger().info(green + "VillagerTradeReset v1.1 enabled successfully!" + reset);
        getLogger().info(cyan + "Status: " + (enabled ? green + "ON" : yellow + "OFF") + reset);
        getLogger().info(cyan + "Execution Mode: " + coloredMode + reset);
        getLogger().info(cyan + "Language set to: " + yellow + lang + reset);
        getLogger().info(cyan + "========================================" + reset);
    }

    @Override
    public void onDisable() {
        String red = "\u001B[31m";
        String reset = "\u001B[0m";
        getLogger().info(red + "VillagerTradeReset v1.1 disabled. See you next time!" + reset);
    }

    private void loadPluginConfig() {
        saveDefaultConfig();
        reloadConfig();
        this.enabled = getConfig().getBoolean("plugin-enable", true);
        this.lang = getConfig().getString("language", "en_US");
    }

    private void determinePlatform() {
        if (classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
            this.isFolia = true;
            this.platformMode = "Folia";
        } else if (classExists("org.purpurmc.purpur.PurpurConfig")) {
            this.isFolia = false;
            this.platformMode = "Purpur";
        } else if (classExists("io.papermc.paper.configuration.Configuration") || classExists("com.destroystokyo.paper.PaperConfig")) {
            this.isFolia = false;
            this.platformMode = "Paper";
        } else if (classExists("org.bukkit.entity.Player$Spigot")) {
            this.isFolia = false;
            this.platformMode = "Spigot";
        } else {
            this.isFolia = false;
            this.platformMode = "Bukkit";
        }
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("vtr")) {
            if (!sender.hasPermission("villagertradereset.admin")) {
                if (sender instanceof Player) sendMsg((Player) sender, "no_perm");
                return true;
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("on")) {
                    this.enabled = true;
                    getConfig().set("plugin-enable", true);
                    saveConfig();
                    sender.sendMessage("§a[VTR] Plugin ON!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("off")) {
                    this.enabled = false;
                    getConfig().set("plugin-enable", false);
                    saveConfig();
                    sender.sendMessage("§c[VTR] Plugin OFF!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    loadPluginConfig();
                    sender.sendMessage("§b[VTR] Config reloaded!");
                    return true;
                }
            }
            sender.sendMessage("§e[VTR] §fUse: §a/vtr on §7| §c/vtr off §7| §b/vtr reload");
            return true;
        }

        if (label.equalsIgnoreCase("resettrades")) {
            if (!(sender instanceof Player)) return true;
            Player player = (Player) sender;
            if (!enabled) {
                sendMsg(player, "disabled");
                return true;
            }
            if (!player.hasPermission("villagertradereset")) {
                sendMsg(player, "no_perm");
                return true;
            }
            Entity target = player.getTargetEntity(5);
            if (target instanceof Villager) {
                runTask(() -> handleReset(player, (Villager) target), target);
            } else {
                sendMsg(player, "look_at");
            }
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("vtr") && args.length == 1) {
            List<String> options = Arrays.asList("on", "off", "reload");
            return options.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @EventHandler
    public void onSneakClick(PlayerInteractEntityEvent event) {
        if (!enabled || !(event.getRightClicked() instanceof Villager)) return;
        Player player = event.getPlayer();
        if (player.isSneaking() && player.hasPermission("villagertradereset") && getConfig().getBoolean("sneak-reset")) {
            event.setCancelled(true);
            runTask(() -> handleReset(player, (Villager) event.getRightClicked()), event.getRightClicked());
        }
    }

    private void handleReset(Player player, Villager villager) {
        Villager.Profession profession = villager.getProfession();
        if (profession == Villager.Profession.NONE || profession == Villager.Profession.NITWIT) {
            sendMsg(player, "no_prof");
            return;
        }
        if (villager.getVillagerExperience() > 0 || villager.getVillagerLevel() > 1) {
            sendMsg(player, "already_traded");
            return;
        }
        villager.setRecipes(new ArrayList<>());
        villager.setVillagerLevel(1);
        villager.setVillagerExperience(0);
        villager.setProfession(Villager.Profession.NONE);
        villager.setProfession(profession);
        sendMsg(player, "reset");
    }

    private void runTask(Runnable task, Entity entity) {
        if (isFolia) entity.getScheduler().run(this, t -> task.run(), null);
        else task.run();
    }

    @SuppressWarnings("deprecation")
    private void sendMsg(Player p, String context) {
        String msg;
        switch (lang) {
            case "pt_BR": case "pt_PT":
                if (context.equals("reset")) msg = "§a§l✔ §aTrocas resetadas!";
                else if (context.equals("no_prof")) msg = "§cEste aldeão não possui uma profissão!";
                else if (context.equals("already_traded")) msg = "§c§l✖ §cVocê já negociou com este aldeão!";
                else if (context.equals("no_perm")) msg = "§c§l✖ §cSem permissão!";
                else if (context.equals("disabled")) msg = "§cO plugin está desativado!";
                else msg = "§cVocê precisa olhar para um aldeão!";
                break;
            case "zh_CN":
                if (context.equals("reset")) msg = "§a§l✔ §a交易已重置！";
                else if (context.equals("no_prof")) msg = "§c该村民没有职业！";
                else if (context.equals("already_traded")) msg = "§c§l✖ §c你已经和这个村民交易过了！";
                else if (context.equals("no_perm")) msg = "§c§l✖ §c没有权限！";
                else if (context.equals("disabled")) msg = "§c插件已禁用！";
                else msg = "§c你需要看着一个村民！";
                break;
            case "es_ES":
                if (context.equals("reset")) msg = "§a§l✔ §a¡Intercambios reiniciados!";
                else if (context.equals("no_prof")) msg = "§c¡Este aldeano no tiene profesión!";
                else if (context.equals("already_traded")) msg = "§c§l✖ §c¡Ya has negociado con este aldeano!";
                else if (context.equals("no_perm")) msg = "§c§l✖ §c¡Sin permiso!";
                else if (context.equals("disabled")) msg = "§c¡El complemento está desactivado!";
                else msg = "§c¡Necesitas mirar a un aldeano!";
                break;
            case "fr_FR":
                if (context.equals("reset")) msg = "§a§l✔ §aÉchanges réinitialisés !";
                else if (context.equals("no_prof")) msg = "§cCe villageois n'a pas de profession !";
                else if (context.equals("already_traded")) msg = "§c§l✖ §cVous avez déjà échangé avec ce villageois !";
                else if (context.equals("no_perm")) msg = "§c§l✖ §cPas de permission !";
                else if (context.equals("disabled")) msg = "§cL'extension est désactivée !";
                else msg = "§cVous devez regarder un villageois !";
                break;
            case "de_DE":
                if (context.equals("reset")) msg = "§a§l✔ §aHandel zurückgesetzt!";
                else if (context.equals("no_prof")) msg = "§cDieser Dorfbewohner hat keinen Beruf!";
                else if (context.equals("already_traded")) msg = "§c§l✖ §cDu hast bereits mit diesem Dorfbewohner gehandelt!";
                else if (context.equals("no_perm")) msg = "§c§l✖ §cKeine Berechtigung!";
                else if (context.equals("disabled")) msg = "§cDas Plugin ist deaktiviert!";
                else msg = "§cDu musst einen Dorfbewohner ansehen!";
                break;
            case "it_IT":
                if (context.equals("reset")) msg = "§a§l✔ §aScambi resettati!";
                else if (context.equals("no_prof")) msg = "§cQuesto villico non ha una professione!";
                else if (context.equals("already_traded")) msg = "§c§l✖ §cHai già scambiato con questo villico!";
                else if (context.equals("no_perm")) msg = "§c§l✖ §cNessun permesso!";
                else if (context.equals("disabled")) msg = "§cIl plugin è disattivato!";
                else msg = "§cDevi guardare un villico!";
                break;
            case "ru_RU":
                if (context.equals("reset")) msg = "§a§l✔ §aТорги сброшены!";
                else if (context.equals("no_prof")) msg = "§cУ этого жителя нет профессии!";
                else if (context.equals("already_traded")) msg = "§c§l✖ §cВы уже торговали с этим жителем!";
                else if (context.equals("no_perm")) msg = "§c§l✖ §cНет прав!";
                else if (context.equals("disabled")) msg = "§cПлагин отключен!";
                else msg = "§cВам нужно смотреть на жителя!";
                break;
            case "ja_JP":
                if (context.equals("reset")) msg = "§a§l✔ §a取引がリセットされました！";
                else if (context.equals("no_prof")) msg = "§cこの村人は職業を持っていません！";
                else if (context.equals("already_traded")) msg = "§c§l✖ §c既にこの村人と取引しています！";
                else if (context.equals("no_perm")) msg = "§c§l✖ §c権限がありません！";
                else if (context.equals("disabled")) msg = "§cプラグインは無効です！";
                else msg = "§c村人を見る必要があります！";
                break;
            case "ko_KR":
                if (context.equals("reset")) msg = "§a§l✔ §a거래가 초기화되었습니다!";
                else if (context.equals("no_prof")) msg = "§c이 주민은 직업이 없습니다!";
                else if (context.equals("already_traded")) msg = "§c§l✖ §c이미 이 주민과 거래했습니다!";
                else if (context.equals("no_perm")) msg = "§c§l✖ §c권한이 없습니다!";
                else if (context.equals("disabled")) msg = "§c플러그인이 비활성화되었습니다!";
                else msg = "§c주민을 바라봐야 합니다!";
                break;
            default:
                if (context.equals("reset")) msg = "§a§l✔ §aTrades reset!";
                else if (context.equals("no_prof")) msg = "§cThis villager has no profession!";
                else if (context.equals("already_traded")) msg = "§c§l✖ §cAlready traded!";
                else if (context.equals("no_perm")) msg = "§c§l✖ §cNo permission!";
                else if (context.equals("disabled")) msg = "§cPlugin is disabled!";
                else msg = "§cLook at a villager!";
                break;
        }
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
}