/*
 * Copyright (c) <2012> <xXLupoXx>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.xXLupoXx.NoSpawn;

import me.xXLupoXx.NoSpawn.Commands.CommandHandler;
import me.xXLupoXx.NoSpawn.Listeners.NoSpawnEntityListener;
import me.xXLupoXx.NoSpawn.Listeners.NoSpawnPlayerListener;
import me.xXLupoXx.NoSpawn.Listeners.NoSpawnWorldListener;
import me.xXLupoXx.NoSpawn.Util.*;
import me.xXLupoXx.NoSpawn.Zones.PlayerSelection;
import me.xXLupoXx.NoSpawn.Zones.ZoneHandler;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;


//TODO Zone speichern, Überprüfen ob Mob in zone, testen

public class NoSpawn extends JavaPlugin {
    private ConfigBuffer cb;
    public   ZoneHandler zoneHandler;
	public MobCounter mc;
    private static NoSpawn plug;

    private  PlayerSelection ps;

	CommandHandler cmh;

    public static NoSpawn getPlugin()
    {
        return plug;
    }


    public PlayerSelection getPlayerSelection()
    {
        return this.ps;
    }

    public ZoneHandler getZoneHandler()
    {
        return zoneHandler;
    }

	public void onEnable() {

        plug = this;
        this.ps = new PlayerSelection();
		this.cb = new ConfigBuffer(this);

		for (World w : this.getServer().getWorlds()) {
			cb.worldSpawns.put(w, new Spawns(cb));
		}

		if(this.getConfig().get("worlds") == null)
		{
			cb.setupConfig();
		}
        cb.checkConfig();

		cb.readConfig();

		cb.plugin = this;
        NoSpawnEntityListener el = new NoSpawnEntityListener(this.cb);
		mc = new MobCounter(this.getServer(), cb);
        NoSpawnWorldListener wl = new NoSpawnWorldListener(cb);
        NoSpawnPlayerListener pl = new NoSpawnPlayerListener(this);

        zoneHandler = new ZoneHandler(this);

        NoSpawnPermissions.setup();

		this.cmh = new CommandHandler(this.getServer(), this.cb);
		PluginDescriptionFile pdfFile = getDescription();



		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");

		getServer().getPluginManager().registerEvents(el, this);
		getServer().getPluginManager().registerEvents(wl, this);
        getServer().getPluginManager().registerEvents(pl, this);

        if (ConfigBuffer.sendMetrics)
        {
            try
            {
                Metrics metrics = new Metrics();

                metrics.beginMeasuringPlugin(this);
            }
            catch (IOException e)
            {
                NoSpawnDebugLogger.debugmsg(e.getMessage());
            }
        }

	}

	public void onDisable() {
		cb.worldSpawns = null;
		getServer().getScheduler().cancelTasks(this);
		PluginDescriptionFile pdfFile = getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion()
				+ " is disabled... Oh my god they are coming!!!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {

		if (command.getName().equals("nospawn")) {

			if (args.length > 0) {
				if (args[0].equals("allowspawn")) {

					return cmh.allowSpawn(sender, args);

				} else if (args[0].equals("denyspawn")) {

					return cmh.denySpawn(sender, args);

				} else if (args[0].equals("despawn")) {

					return cmh.despawnMobs(sender, args);

				} else if (args[0].equals("setmoblimit")) {

					return cmh.setMobLimit(sender, args);

				} else if (args[0].equals("settotalmoblimit")) {

					return cmh.setTotalMobLimit(sender, args);

				} else if (args[0].equals("settimer")) {

					return cmh.setMobTimer(sender, args);

				} else if (args[0].equals("usegbbl")) {

                    return cmh.setUseGlobalBlockBlacklist(sender, args);

                } else if (args[0].equals("addgbbl")|| args[0].equals("delgbbl")) {

                    return cmh.editGlobalBlockBlacklist(sender, args);

                } else if (args[0].equals("addbl")|| args[0].equals("delbl")) {

                    return cmh.editBlockBlacklist(sender, args);

                } else if (args[0].equals("reloadconf")) {

                    return cmh.reloadConf(sender);

                } else if(args[0].equals("debug")) {

                    ConfigBuffer.Debugmode = !ConfigBuffer.Debugmode;
                    return true;

                } else if(args[0].equals("zcreate")) {

                    return cmh.createZone(sender,args);

                } else {

					sendNospawnMessage(
							sender,
							args[0]
									+ " isn't a valid parameter! Please use allowspawn, denyspawn, despawn, setmoblimit, settotalmoblimit," +
                                    " usegbbl, addgbbl. delgbbl, addbl, delbl, reloadconf or settimer ",
							ChatColor.RED);
					return false;

				}
			} else {

				sendNospawnMessage(
						sender,
						"No arguments given. Please use allowspawn, denyspawn or despawn, setmoblimit, settotalmoblimit" +
                                " usegbbl, addgbbl. delgbbl, addbl, delbl, reloadconf or settimer ",
						ChatColor.RED);

			}
		}

		return false;
	}


	public void sendNospawnMessage(CommandSender sender, String Message,
			ChatColor Color) {

		if (sender instanceof Player) {

			Player player = (Player) sender;
			player.sendMessage(Color + Message);

		} else {

			System.out.println("[NoSpawn] " + Message);

		}

	}

}