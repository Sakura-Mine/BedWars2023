package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.server.ServerType;
import com.andrei1058.bedwars.arena.*;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.commands.bedwars.subcmds.regular.CmdStats;
import com.andrei1058.bedwars.configuration.Permissions;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.language.PreLoadedLanguage;
import com.andrei1058.bedwars.lobbysocket.LoadedUser;
import com.andrei1058.bedwars.sidebar.BedWarsScoreboard;
import com.andrei1058.bedwars.support.preloadedparty.PreLoadedParty;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.UUID;

import static com.andrei1058.bedwars.BedWars.*;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class QuitAndTeleportListener implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        // Remove from arena
        IArena a = Arena.getArenaByPlayer(p);
        if (a != null) {
            if (a.isPlayer(p)) {
                a.removePlayer(p, true);
            } else if (a.isSpectator(p)) {
                a.removeSpectator(p, true);
            }
        }

        //Save preferred language
        if (Language.getLangByPlayer().containsKey(p)) {
            final UUID u = p.getUniqueId();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                String iso = Language.getLangByPlayer().get(p).getIso();
                if (Language.isLanguageExist(iso)) {
                    if (BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_DISABLED_LANGUAGES).contains(iso))
                        iso = Language.getDefaultLanguage().getIso();
                    BedWars.getRemoteDatabase().setLanguage(u, iso);
                }
                Language.getLangByPlayer().remove(p);
            });
        }

        if (getServerType() != ServerType.SHARED) {
            e.setQuitMessage(null);
        }
        // Manage internal parties
        if (getParty().isInternal()) {
            if (getParty().hasParty(p)) {
                getParty().removeFromParty(p);
            }
        }
        // Check if was doing a setup and remove the session
        SetupSession ss = SetupSession.getSession(p.getUniqueId());
        if (ss != null) {
            ss.cancel();
        }

        BedWarsScoreboard sb = BedWarsScoreboard.getSBoard(e.getPlayer().getUniqueId());
        if (sb != null) {
            sb.remove();
        }

        BedWarsTeam.antiFallDamageAtRespawn.remove(e.getPlayer().getUniqueId());

        LastHit lh = LastHit.getLastHit(p);
        if (lh != null) {
            lh.remove();
        }

        CmdStats.getStatsCoolDown().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e == null) return;
        if (e.isCancelled()) return;
        if (e.getTo() == null) return;
        if (e.getTo().getWorld() == null) return;
        IArena a = Arena.getArenaByPlayer(e.getPlayer());
        if (a != null) {
            IArena a1 = Arena.getArenaByIdentifier(e.getTo().getWorld().getName());
            if (a1 != null) {
                if (!a1.equals(a)) {
                    if (a.isSpectator(e.getPlayer())) a.removeSpectator(e.getPlayer(), false);
                    if (a.isPlayer(e.getPlayer())) a.removePlayer(e.getPlayer(), false);
                    e.getPlayer().sendMessage("PlayerTeleportEvent something went wrong. You have joined an arena world while playing on a different map.");
                }
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        // I think this for shared mode should be removed
        if (BedWars.getServerType() == ServerType.SHARED) {
            if (BedWars.config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_LOBBY_SIDEBAR)) {
                //Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                if (e.getPlayer().getWorld().getName().equalsIgnoreCase(BedWars.getLobbyWorld())) {
                    BedWarsScoreboard.giveScoreboard(e.getPlayer(), null, true);
                } else {
                    BedWarsScoreboard sb = BedWarsScoreboard.getSBoard(e.getPlayer().getUniqueId());
                    if (sb != null) {
                        sb.remove();
                    }
                }
                //}, 2L);
            }
        }
        if (Arena.isInArena(e.getPlayer())) {
            IArena a = Arena.getArenaByPlayer(e.getPlayer());
            if (a.isPlayer(e.getPlayer())) {
                if (a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting) return;
                if (!e.getPlayer().getWorld().getName().equalsIgnoreCase(a.getWorld().getName())) {
                    a.removePlayer(e.getPlayer(), BedWars.getServerType() == ServerType.BUNGEE);
                    debug(e.getPlayer().getName() + " was removed from " + a.getDisplayName() + " because he was teleported outside the arena.");
                }
            }
        }
    }
}
