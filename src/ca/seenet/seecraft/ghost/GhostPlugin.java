package ca.seenet.seecraft.ghost;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Have you seen my ghost?
 *
 * @author scoleman
 */
public class GhostPlugin extends JavaPlugin implements Listener
{
    private Set<String> ghosts;

    @Override
    public void onEnable()
    {
        this.ghosts = new HashSet<String>();
        super.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
        this.unghostAll();
        HandlerList.unregisterAll((JavaPlugin) this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!sender.isOp())
        {
            sender.sendMessage("Go away.");
            return true;
        }

        /* We can't do anything without arguments. */
        if (args.length < 1)
            return false;

        /* If we have enough arguments to include a username, grab the named player. */
        Player ghost = null;
        if (args.length >= 2)
            ghost = this.getServer().getPlayer(args[1]);

        /* Build a list of ghosts. */
        if (args[0].equalsIgnoreCase("list"))
        {
            sender.sendMessage(this.ghostList());
        }
        /* Add a new ghost. */
        else if (args[0].equalsIgnoreCase("add"))
        {
            if (ghost == null)
                return false;
            this.ghost(ghost);
        }
        /* Remove a ghost. */
        else if (args[0].equalsIgnoreCase("remove"))
        {
            if (ghost == null)
                return false;
            this.unghost(ghost);
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerLoginEvent event)
    {
        this.fixVisibility();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        this.ghost(event.getEntity());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (this.ghosts.contains(event.getPlayer().getName().toLowerCase()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (this.ghosts.contains(event.getPlayer().getName().toLowerCase()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onItem(PlayerPickupItemEvent event)
    {
        if (this.ghosts.contains(event.getPlayer().getName().toLowerCase()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntity(PlayerInteractEntityEvent event)
    {
        if (this.ghosts.contains(event.getPlayer().getName().toLowerCase()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event)
    {
        for (String username : this.ghosts)
        {
            Player player = super.getServer().getPlayer(username);
            if (player == null)
                continue;

            if (player.getEntityId() == event.getDamager().getEntityId()
                    || player.getEntityId() == event.getEntity().getEntityId())
                event.setCancelled(true);
        }
    }

    private String ghostList()
    {
        if (this.ghosts.size() == 0)
            return "There are 0 ghosts.";

        String ghostsList;
        if (this.ghosts.size() == 1)
            ghostsList = "There is 1 ghost:\n";
        else
            ghostsList = "There are " + this.ghosts.size() + " ghosts:\n";

        boolean hasPlayer = false;
        for (String username : this.ghosts)
        {
            if (hasPlayer)
                ghostsList += " ";
            ghostsList += username;
            hasPlayer = true;
        }

        return ghostsList;
    }

    private void ghost(Player player)
    {
        /* You can't kill that which is already dead. */
        if (this.ghosts.contains(player.getName().toLowerCase()))
            return;

        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);

        this.ghosts.add(player.getName().toLowerCase());
        this.fixVisibility();
    }

    private void unghost(Player player)
    {
        /* If the player isn't a ghost, there's not much we can do. */
        if (!this.ghosts.contains(player.getName().toLowerCase()))
            return;

        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);

        this.ghosts.remove(player.getName().toLowerCase());
        this.fixVisibility();
    }

    private void unghostAll()
    {
        for (String username : this.ghosts)
        {
            Player player = super.getServer().getPlayer(username);
            if (player == null)
                continue;

            player.setAllowFlight(false);
            player.setGameMode(GameMode.SURVIVAL);
        }

        this.ghosts.clear();
        this.fixVisibility();
    }

    private void fixVisibility()
    {
        Player players[] = super.getServer().getOnlinePlayers();

        for (Player player : players)
            if (this.ghosts.contains(player.getName().toLowerCase()))
                for (Player otherPlayer : players)
                    player.showPlayer(otherPlayer);
            else
                for (Player otherPlayer : players)
                    if (ghosts.contains(otherPlayer.getName().toLowerCase()))
                        player.hidePlayer(otherPlayer);
                    else
                        player.showPlayer(otherPlayer);
    }
}
