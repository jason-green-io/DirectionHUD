package one.oth3r.directionhud.common;

import one.oth3r.directionhud.common.files.PlayerData;
import one.oth3r.directionhud.common.files.config;
import one.oth3r.directionhud.DirectionHUD;
import one.oth3r.directionhud.common.utils.Helper;
import one.oth3r.directionhud.common.utils.Helper.Enums;
import one.oth3r.directionhud.common.utils.Helper.ListPage;
import one.oth3r.directionhud.common.utils.Helper.Command.Suggester;
import one.oth3r.directionhud.common.utils.Loc;
import one.oth3r.directionhud.utils.CTxT;
import one.oth3r.directionhud.common.utils.CUtl;
import one.oth3r.directionhud.utils.Player;
import one.oth3r.directionhud.utils.Utl;

import java.util.*;

public class DHUD {
    private static CTxT lang(String key, Object... args) {
        return CUtl.getLangEntry("dhud."+key, args);
    }
    public static void CMDExecutor(Player player, String[] args) {
        if (args.length == 0) {
            UI(player);
            return;
        }
        String type = args[0].toLowerCase();
        String[] trimmedArgs = Helper.trimStart(args, 1);
        switch (type) {
            case "inbox" -> inbox.CMDExecutor(player,trimmedArgs);
            case "color" -> preset.colorCMDExecutor(player,trimmedArgs);
            case "presets", "preset" -> preset.CMDExecutor(player,trimmedArgs);
            case "reload" -> {
                // make sure the player can reload
                if (Utl.checkEnabled.reload(player)) reload(player);
            }
            // hud and dest redirect
            case "dest", "destination" -> Destination.commandExecutor.logic(player,trimmedArgs);
            case "hud" -> HUD.CMDExecutor(player,trimmedArgs);
            default -> player.sendMessage(CUtl.error("command"));
        }
    }
    public static ArrayList<String> CMDSuggester(Player player, int pos, String[] args) {
        ArrayList<String> suggester = new ArrayList<>();
        if (!Utl.checkEnabled.hud(player)) return suggester;
        if (pos == 1) {
            if (config.social) suggester.add("inbox");
            if (Utl.checkEnabled.reload(player)) suggester.add("reload");
            if (Utl.checkEnabled.destination(player)) suggester.add("dest");
            if (Utl.checkEnabled.hud(player)) suggester.add("hud");
            if (Utl.checkEnabled.customPresets(player)) suggester.add("preset");
        }
        if (pos > 1) {
            String command = args[0].toLowerCase();
            // trim the start
            String[] trimmedArgs = Helper.trimStart(args, 1);
            // fix the pos
            int fixedPos = pos - 2;
            switch (command) {
                case "dest","destination" -> suggester.addAll(Destination.commandSuggester.logic(player,fixedPos+1,trimmedArgs));
                case "hud" -> suggester.addAll(HUD.CMDSuggester(player,fixedPos+1,trimmedArgs));
                case "preset" -> suggester.addAll(preset.CMDSuggester(player,fixedPos,trimmedArgs));
                case "color" -> {
                    if (fixedPos == 4) suggester.addAll(Suggester.colors(player,Suggester.getCurrent(trimmedArgs,fixedPos)));
                }
            }
        }
        return suggester;
    }
    public static CTxT RELOAD_BUTTON = lang("button.reload").btn(true).color(Assets.mainColors.reload)
            .cEvent(1,"/dhud reload")
            .hEvent(CTxT.of(Assets.cmdUsage.reload).color(Assets.mainColors.reload).append("\n").append(lang("hover.reload")));
    /**
     * reloads DirectionHUD
     * @param player null if reloading from the console
     */
    public static void reload(Player player) {
        config.load();
        // fully reload the players
        for (Player pl: Utl.getPlayers()) {
            Events.playerSoftLeave(pl);
            Events.playerJoin(pl);
        }
        if (player == null) DirectionHUD.LOGGER.info(lang("msg.reload").toString());
        else player.sendMessage(CUtl.tag().append(lang("msg.reload").color('a')));
    }
    public static class inbox {
        public static final int PER_PAGE = 3;
        private static CTxT lang(String key, Object... args) {
            return DHUD.lang("inbox."+key, args);
        }
        public static CTxT BUTTON = lang("button").btn(true).color(Assets.mainColors.inbox)
                .cEvent(1,"/dhud inbox")
                .hEvent(CTxT.of(Assets.cmdUsage.inbox).color(Assets.mainColors.inbox).append("\n").append(lang("hover")));
        public static void CMDExecutor(Player player, String[] args) {
            if (!config.social) return;
            // UI
            if (args.length <= 1) {
                if (args.length == 0) UI(player,1);
                else UI(player, Helper.Num.toInt(args[0]));
                return;
            }
            // DELETING
            if (args[0].equalsIgnoreCase("clear") && args.length == 2) {
                delete(player,args[1],true);
                return;
            }
            player.sendMessage(CUtl.usage(Assets.cmdUsage.inbox));
        }
        public enum Type {
            track_pending,
            track_request,
            destination
        }

        /**
         * counts down all expire clocks in the inbox
         */
        public static void tick(Player player) {
            ArrayList<HashMap<String, Object>> inbox = PlayerData.get.inbox(player);
            // iterate over the arraylist, as we are editing it, cant use for loop
            Iterator<HashMap<String, Object>> iterator = inbox.iterator();
            while (iterator.hasNext()) {
                HashMap<String, Object> entry = iterator.next();
                double expire = (double) entry.get("expire");
                // tick the "expire" value
                entry.put("expire", String.valueOf(expire-1));
                // remove from inbox when expire is 0
                if (expire <= 0) {
                    iterator.remove();
                    // send expire messages if pending expired
                    if (entry.get("type").equals(Type.track_pending.name())) {
                        Player target = Player.of((String) entry.get("player_name"));
                        if (target==null) continue;
                        target.sendMessage(CUtl.tag().append(Destination.social.track.LANG.msg("expired.target", player.getHighlightedName())));
                        player.sendMessage(CUtl.tag().append(Destination.social.track.LANG.msg("expired",target.getHighlightedName())));
                    }
                }
            }
            PlayerData.set.inbox(player,inbox);
        }
        /**
         * removes all entries to deal with tracking, because tracking entries doesn't save between sessions
         */
        public static void removeAllTracking(Player player) {
            // removes all pending and requests from the player and their targets
            ArrayList<HashMap<String, Object>> inbox = PlayerData.get.inbox(player);
            // iterate over the arraylist, as we are editing it, cant use for loop
            Iterator<HashMap<String, Object>> iterator = inbox.iterator();
            while (iterator.hasNext()) {
                HashMap<String, Object> entry = iterator.next();
                // if pending or request, clear both from player and the target player (sync)
                if (entry.get("type").equals(Type.track_pending.name()) || entry.get("type").equals(Type.track_request.name())) {
                    // get the second type to search for in the target player (the opposite type of the player)
                    Type type = entry.get("type").equals(Type.track_pending.name())?
                            Type.track_request : Type.track_pending;
                    // use name if online mode is off
                    Player target = Player.of((String)entry.get("player_uuid"));
                    if (!config.online) target = Player.of((String)entry.get("player_name"));
                    if (target != null) {
                        // search for the opposite type of the player and the id to match it in target inbox and remove
                        removeEntry(target, one.oth3r.directionhud.common.DHUD.inbox.search(target, type,"id",entry.get("id")));
                    }
                    //remove from player
                    iterator.remove();
                }
            }
            PlayerData.set.inbox(player,inbox);
        }
        /**
         * searches all player entries for a matching key and value from a certain type
         * @param type null to search all types, otherwise only searches a certain type of entry
         * @param key the key to search
         * @param value the value to match
         * @return the first entry that contains the key and value
         */
        public static HashMap<String, Object> search(Player player, Type type, String key, Object value) {
            ArrayList<HashMap<String, Object>> inbox = PlayerData.get.inbox(player);
            for (HashMap<String, Object> entry: inbox) {
                // if the type isn't null, and it doesn't match, continue to the next entry
                if (type!=null && !entry.get("type").equals(type.name())) continue;
                if (entry.get(key).equals(value)) return entry;
            }
            return null;
        }
        /**
         * gets all the entries from a certain type
         * @param type the type of entry to search for
         * @return null if none found, the list of entries if there are any
         */
        public static ArrayList<HashMap<String, Object>> getAllType(Player player, Type type) {
            ArrayList<HashMap<String, Object>> inbox = PlayerData.get.inbox(player);
            ArrayList<HashMap<String, Object>> matches = new ArrayList<>();
            for (HashMap<String, Object> entry: inbox)
                if (entry.get("type").equals(type.name())) matches.add(0,entry);
            if (!matches.isEmpty()) return matches;
            return null;
        }
        /**
         * creates a tracking request and pending entry to both the sender and target
         * @param target the player that is going to get tracked
         * @param from the player that is sending the tracking request
         * @param time the amount of time that the entry is going to last
         */
        public static void addTracking(Player target, Player from, int time) {
            String ID = Helper.createID();
            // create the track request for the target
            ArrayList<HashMap<String, Object>> inbox = PlayerData.get.inbox(target);
            HashMap<String, Object> entry = new HashMap<>();
            entry.put("type", Type.track_request);
            entry.put("player_name",from.getName());
            entry.put("player_uuid",from.getUUID());
            entry.put("id",ID);
            entry.put("expire",time);
            inbox.add(0,entry);
            PlayerData.set.inbox(target,inbox);
            // create the track pending for the requester
            inbox = PlayerData.get.inbox(from);
            entry = new HashMap<>();
            entry.put("type", Type.track_pending);
            entry.put("player_name",target.getName());
            entry.put("player_uuid",target.getUUID());
            entry.put("id",ID);
            entry.put("expire",time);
            inbox.add(0,entry);
            PlayerData.set.inbox(from,inbox);
        }
        /**
         * adds a destination to the target player's inbox
         * @param target target player
         * @param from the player who sent the destination
         * @param time how long the entry should last
         * @param loc the destination location
         */
        public static void addDest(Player target, Player from, int time, Loc loc) {
            if (!loc.hasDestRequirements()) return;
            ArrayList<HashMap<String, Object>> inbox = PlayerData.get.inbox(target);
            HashMap<String, Object> entry = new HashMap<>();
            entry.put("type", Type.destination.name());
            entry.put("player_name",from.getName());
            entry.put("player_uuid",from.getUUID());
            entry.put("id", Helper.createID());
            entry.put("expire",String.valueOf(time));
            entry.put("loc",loc.toString());
            // add to the top of the list
            inbox.add(0,entry);
            // save the inbox
            PlayerData.set.inbox(target,inbox);
        }
        /**
         * removes the entry provided
         * @param entry the entry to remove
         */
        public static void removeEntry(Player player, HashMap<String, Object> entry) {
            if (entry == null) return;
            ArrayList<HashMap<String, Object>> inbox = PlayerData.get.inbox(player);
            inbox.remove(entry);
            PlayerData.set.inbox(player,inbox);
        }
        /**
         * delete an entry via ID
         * @param ID the id of the entry to remove
         * @param playerBased if requested by the player, to send a message and return or not
         */
        public static void delete(Player player, String ID, boolean playerBased) {
            Helper.ListPage<HashMap<String, Object>> listPage = new Helper.ListPage<>(PlayerData.get.inbox(player),PER_PAGE);
            //delete via ID (command)
            HashMap<String, Object> entry = search(player,null,"id",ID);
            // stop if there's nothing to clear
            if (entry==null) return;
            // remove the entry
            removeEntry(player,entry);
            if (playerBased) {
                player.sendMessage(CUtl.tag().append(lang("msg.cleared",CTxT.of((String)entry.get("player_name")).color(CUtl.s()))));
                UI(player,listPage.getPageOf(entry));
            }
        }
        /**
         * makes the TxT for the entry provided
         * @param entry entry data
         * @return the TxT created
         */
        public static CTxT getEntryTxT(Player player, HashMap<String, Object> entry) {
            // get the entry type
            Type type = Enums.get(entry.get("type"),Type.class);
            // get the entry name
            String name = (String)entry.get("player_name");
            // get name from UUID if online mode is on
            if (config.online) {
                Player player_uuid = Player.of((String)entry.get("player_uuid"));
                if (player_uuid != null) name = player_uuid.getName();
            }
            // make the TxTs that make things easier
            CTxT msg = CTxT.of(""),
                    time = lang("ui.time",((Double)entry.get("expire")).intValue()).color('7'),
                    from = lang("ui.from",CTxT.of(name).color(CUtl.s())),
                    to = lang("ui.to",CTxT.of(name).color(CUtl.s()));
            // switch for the different type of entries
            switch (type) {
                case track_request ->
                        msg.append(lang("ui.track_request",time).color(CUtl.p())).append(" ")
                            // to / from
                            .append("\n  ").append(from).append("\n   ")
                            // accept & deny buttons
                            .append(CUtl.button("accept").btn(true).color('a')
                                    .hEvent(CUtl.hover("accept").color('a'))
                                    .cEvent(1,"/dest track accept-r "+name)).append(" ")
                            .append(CUtl.button("deny").btn(true).color('c')
                                    .hEvent(CUtl.hover("deny").color('c'))
                                    .cEvent(1,"/dest track deny-r "+name));
                case track_pending ->
                        msg.append(lang("ui.track_pending",time).color(CUtl.p())).append(" ")
                            // to / from
                            .append("\n  ").append(to).append("\n   ")
                            // cancel button
                            .append(CUtl.button("cancel").btn(true).color('c')
                                    .hEvent(CUtl.hover("cancel").color('c'))
                                    .cEvent(1, "/dest track cancel-r "+name));
                case destination ->
                        msg.append(lang("ui.destination",time).color(CUtl.p())).append(" ")
                            // x button
                            .append(CTxT.of(Assets.symbols.x).btn(true).color('c')
                                    .hEvent(lang("hover.clear").color('c'))
                                    .cEvent(1,"/dhud inbox clear "+entry.get("id")))
                            // to / from
                            .append("\n  ").append(from).append("\n   ")
                            // destination badge
                            .append(Destination.social.send.getSendTxt(player,new Loc(entry.get("loc").toString())));
            }
            return msg;
        }
        public static void UI(Player player, int pg) {
            Helper.ListPage<HashMap<String, Object>> listPage = new Helper.ListPage<>(PlayerData.get.inbox(player),PER_PAGE);
            CTxT msg = CTxT.of(" ").append(lang("ui").color(Assets.mainColors.inbox)).append(CUtl.LINE_35).append("\n ");
            for (HashMap<String, Object> index : listPage.getPage(pg)) {
                msg.append(getEntryTxT(player,index)).append("\n ");
            }
            // no entries
            if (listPage.getList().isEmpty()) msg.append("\n ").append(lang("ui.empty").color('7').italic(true)).append("\n");
            // bottom row
            msg.append("\n ")
                    .append(listPage.getNavButtons(pg,"/dhud inbox ")).append(" ")
                    .append(CUtl.CButton.back("/dhud")).append(CUtl.LINE_35);
            player.sendMessage(msg);
        }
    }
    public static class preset {
        private static final int PER_PAGE = 7;
        public static final String DEFAULT_UI_SETTINGS = "normal";
        public static CTxT lang(String key, Object... args) {
            return DHUD.lang("preset."+key, args);
        }
        public static CTxT error(String key, Object... args) {
            return CUtl.error().append(lang("error."+key, args));
        }
        public static CTxT BUTTON = lang("button").btn(true).color(Assets.mainColors.presets)
                .cEvent(1,"/dhud preset")
                .hEvent(CTxT.of("/dhud presets").color(Assets.mainColors.presets).append("\n").append(lang("hover")));
        public static void colorCMDExecutor(Player player, String[] args) {
            if (args.length != 5) return;
            // /dhud color (settings) (type) (subtype) (set/preset) (color/page)
            if (Enums.toStringList(Enums.toArrayList(Type.values())).contains(args[1])) {
                Type type = Type.get(args[1]);
                if (args[3].equals("set")) setColor(player,args[0],type,args[2],args[4]);
                if (args[3].equals("preset")) UI(player,args[0],type,args[2],args[4]);
            }
        }
        public static void CMDExecutor(Player player, String[] args) {
            if (!Utl.checkEnabled.customPresets(player)) return;
            if (args.length <= 1) {
                // preset ui
                if (args.length == 0) custom.UI(player,1,null);
                    // via page num
                else if (Helper.Num.isNum(args[0])) custom.UI(player,Helper.Num.toInt(args[0]),null);
                    // via preset name
                else {
                    ArrayList<String> presets = PlayerData.get.colorPresets(player);
                    ListPage<String> listPage = new ListPage<>(presets, PER_PAGE);
                    // check if the preset is valid, then get the page for that preset
                    if (custom.getNames(presets).contains(args[0])) {
                        String preset = args[0] +"|"+ custom.getColors(presets).get(custom.getNames(presets).indexOf(args[0]));
                        custom.UI(player, listPage.getPageOf(preset), null);
                    }
                }
                return;
            }
            boolean Return = false;
            // if the type has -r, remove it and enable returning
            if (args[0].contains("-r")) {
                args[0] = args[0].replace("-r","");
                Return = true;
            }
            if (args.length == 2 && args[0].equals("delete")) custom.delete(player,args[1],Return);
            if (args.length == 3) {
                if (args[0].equals("save")) custom.save(player,args[2],args[1],Return);
                if (args[0].equals("rename")) custom.rename(player,args[1],args[2],Return);
                if (args[0].equals("colorui")) custom.colorUI(player,args[2],args[1],null);
                if (args[0].equals("color")) custom.setColor(player,"",args[1],args[2],Return);
            }
        }
        public static ArrayList<String> CMDSuggester(Player player, int pos, String[] args) {
            ArrayList<String> suggester = new ArrayList<>();
            if (!Utl.checkEnabled.customPresets(player)) return suggester;
            /*
               preset rename (name) (newName)
               preset save (color) (name)
               preset color (name) (color)
               preset delete (name)
             */
            if (pos == 0) {
                suggester.add("rename");
                suggester.add("save");
                suggester.add("color");
                suggester.add("delete");
                return suggester;
            }
            // if -r is attached, remove it and continue with the suggester
            if (args[0].contains("-r")) args[0] = args[0].replace("-r", "");
            if (args[0].equals("save")) {
                if (pos == 1) return Suggester.colors(player,Suggester.getCurrent(args,pos));
                if (pos == 2) suggester.add("name");
            } else {
                if (pos == 1) suggester.addAll(Suggester.wrapQuotes(custom.getNames(PlayerData.get.colorPresets(player))));
                else if (args[0].equals("rename")) suggester.add("name");
                else if (args[0].equals("color")) return Suggester.colors(player,Suggester.getCurrent(args,pos));
            }
            return suggester;
        }
        public enum Type {
            hud,
            dest,
            saved,
            preset,
            unknown;
            public static Type get(String s) {
                try {
                    return Type.valueOf(s);
                } catch (IllegalArgumentException e) {
                    return unknown;
                }
            }
        }
        /**
         * sets color via the Type, using the setColor defined by each color type, returns to the UI
         * @param UISettings color UI settings
         * @param type color type
         * @param subtype color subtype
         * @param color the color to set
         */
        public static void setColor(Player player, String UISettings, Type type, String subtype, String color) {
            // /dhud color (settings) (type) (subtype) set (color)
            switch (type) {
                case hud -> {
                    HUD.color.setColor(player,UISettings,subtype,color,true);
                }
                case dest -> {
                    Destination.settings.setColor(player,UISettings,
                            Destination.Setting.get(subtype),color,true);
                }
                case saved -> {
                    // if using dhud set, its always local destinations
                    Destination.saved.setColor(player,new Destination.saved.Dest(player,subtype,false),
                            UISettings,color,true);
                }
                case preset -> {
                    custom.setColor(player,UISettings,subtype,color,true);
                }
            }
        }
        /**
         * displays the color editor with the provided settings & type
         * @param color current color to edit
         * @param UISettings color UI settings
         * @param type type of color
         * @param subtype subtype of color
         * @param stepCMD the command to change the step size
         * @return the color editor
         */
        public static CTxT colorEditor(String color, String UISettings, Type type, String subtype, String stepCMD) {
            CTxT presetsButton = CTxT.of("")
                    .append(CTxT.of("+").btn(true).color('a')
                            .cEvent(2,String.format("/dhud preset save \"%s\" ",color))
                            .hEvent(lang("hover.preset.plus",lang("hover.preset.plus_2").color(color))))
                    .append(lang("button.preset").color(Assets.mainColors.presets)
                            .cEvent(1,String.format("/dhud color %s %s \"%s\" preset default",UISettings,type,subtype)).btn(true)
                            .hEvent(lang("hover.preset.editor",lang("hover.preset.editor_2").color(Assets.mainColors.presets))));
            CTxT customButton = lang("button.custom").btn(true).color(Assets.mainColors.custom)
                    .cEvent(2,String.format("/dhud color %s %s \"%s\" set ",UISettings,type,subtype))
                    .hEvent(lang("hover.custom",lang("hover.custom.2").color(Assets.mainColors.custom)));
            CTxT defaultSquare = CTxT.of(Assets.symbols.square).color(color).hEvent(CUtl.color.getBadge(color)),
                    smallButton = lang("editor.step.button.small").color(CUtl.s()).cEvent(1,String.format(stepCMD,"small"))
                            .hEvent(lang("editor.step.hover",lang("editor.step.button.small").color(CUtl.s()))).btn(true),
                    normalButton = lang("editor.step.button.normal").color(CUtl.s()).cEvent(1,String.format(stepCMD,"normal"))
                            .hEvent(lang("editor.step.hover",lang("editor.step.button.normal").color(CUtl.s()))).btn(true),
                    bigButton = lang("editor.step.button.big").color(CUtl.s()).cEvent(1,String.format(stepCMD,"big"))
                            .hEvent(lang("editor.step.hover",lang("editor.step.button.big").color(CUtl.s()))).btn(true);
            // initialize the change amounts for each step size
            float[] changeAmounts = new float[3];
            if (UISettings == null || UISettings.equals("normal")) {
                normalButton.color(Assets.mainColors.gray).cEvent(1,null).hEvent(null);
                changeAmounts[0] = 0.02f;
                changeAmounts[1] = 0.05f;
                changeAmounts[2] = 0.1f;
            } else if (UISettings.equals("small")) {
                smallButton.color(Assets.mainColors.gray).cEvent(1,null).hEvent(null);
                changeAmounts[0] = 0.005f;
                changeAmounts[1] = 0.0125f;
                changeAmounts[2] = 0.025f;
            } else if (UISettings.equals("big")) {
                bigButton.color(Assets.mainColors.gray).cEvent(1,null).hEvent(null);
                changeAmounts[0] = 0.04f;
                changeAmounts[1] = 0.1f;
                changeAmounts[2] = 0.2f;
            }
            ArrayList<CTxT> hsbList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                hsbList.add(CTxT.of("-").btn(true));
                hsbList.add(CTxT.of("+").btn(true));
            }
            int i = 0;
            // todo color editor logic, dissect later i was dumb and didn't comment whyy...
            for (int changeAmt = 0; changeAmt < 3;changeAmt++) {
                for (int plus = i;plus < i+2;plus++) {
                    String editedColor = CUtl.color.editHSB(changeAmt,color,(plus%2==0)?changeAmounts[changeAmt]*-1:(changeAmounts[changeAmt]));
                    hsbList.get(plus).color(editedColor.equals(color)?Assets.mainColors.gray:editedColor);
                    if (!editedColor.equals(color)) {
                        hsbList.get(plus).hEvent(lang("color.hover.set",CUtl.color.getBadge(editedColor)));
                        hsbList.get(plus).cEvent(1,String.format("/dhud color %s %s \"%s\" set \"%s\"",UISettings,type,subtype,editedColor));
                    }
                }
                i = i+2;
            }
            return CTxT.of(" ")
                    .append(presetsButton).append(" ").append(customButton).append("\n\n")
                    .append("  ")
                    .append(hsbList.get(0)).append(" ").append(defaultSquare).append(" ").append(hsbList.get(1)).append(" ").append(lang("editor.hue")).append("\n  ")
                    .append(hsbList.get(2)).append(" ").append(defaultSquare).append(" ").append(hsbList.get(3)).append(" ").append(lang("editor.saturation")).append("\n  ")
                    .append(hsbList.get(4)).append(" ").append(defaultSquare).append(" ").append(hsbList.get(5)).append(" ").append(lang("editor.brightness")).append("\n\n ")
                    .append(smallButton).append(" ").append(normalButton).append(" ").append(bigButton);
        }
        /**
         * the presets UI
         * @param UISettings color UI settings
         * @param type type of color
         * @param subtype subtype of color
         * @param page the page to display
         */
        public static void UI(Player player, String UISettings, Type type, String subtype, String page) {
            // top button initialization
            String clickCMD = String.format("/dhud color %s %s \"%s\" ",UISettings,type,subtype);
            CTxT defaultBtn = lang("button.default").color(CUtl.s()).cEvent(1,clickCMD+"preset default").btn(true),
                    minecraftBtn = lang("button.minecraft").color(CUtl.s()).cEvent(1,clickCMD+"preset minecraft").btn(true),
                    customBtn = CTxT.of(" ").append(lang("button.custom").color(CUtl.s()).cEvent(1,clickCMD+"preset custom").btn(true)), // space at start for alignment
                    list = CTxT.of(""); // text for inside the UI
            // code for the button selector page, default and mc colors
            if (page.equals("default") || page.equals("minecraft")) {
                List<String> colorStrings, colors;
                int rowAmt;
                if (page.equals("default")) {
                    // disable the current page button & set the data for the loop
                    defaultBtn.color(Assets.mainColors.gray).cEvent(1,null).hEvent(null);
                    colorStrings = List.of("red","orange","yellow","green","blue","purple","gray");
                    colors = CUtl.color.DEFAULT_COLORS;
                    rowAmt = 3;
                } else {
                    minecraftBtn.color(Assets.mainColors.gray).cEvent(1,null).hEvent(null);
                    colorStrings = List.of("red","yellow","green","aqua","blue","purple","gray");
                    colors = List.of("#FF5555","#AA0000",
                            "#FFFF55","#FFAA00",
                            "#55FF55","#00AA00",
                            "#55FFFF","#00AAAA",
                            "#5555FF","#0000AA",
                            "#FF55FF","#AA00AA",
                            "#AAAAAA","#555555");
                    rowAmt = 2;
                }
                int colorIndex = 0;
                // for all the preset color types
                for (String s : colorStrings) {
                    list.append("\n ");
                    // for x amt of colors per type
                    for (int i = 0; i < rowAmt;i++) {
                        String color = colors.get(colorIndex);
                        list.append(CTxT.of(Assets.symbols.square).btn(true).color(color)
                                .cEvent(1,String.format(clickCMD+" set \"%s\"",color))
                                .hEvent(lang("color.hover.set", CUtl.color.getBadge(color))));
                        colorIndex++;
                    }
                    list.append(" ").append(lang("color."+s));
                }
            } else {
                // custom, just numbers for the pages instead of an identifier, easier that way trust me
                int pg = Helper.Num.toInt(page);
                ListPage<String> listPage = new ListPage<>(PlayerData.get.colorPresets(player),7);
                customBtn = listPage.getNavButtons(pg,clickCMD+"preset ");
                for (String preset : listPage.getPage(pg)) {
                    String color = custom.getColor(preset), name = custom.getName(preset);
                    list.append("\n ").append(CTxT.of(Assets.symbols.square).color(color).btn(true)
                                    .cEvent(1,String.format(clickCMD+" set \"%s\"",color))
                                    .hEvent(lang("color.hover.set",CUtl.color.getBadge(color))))
                            .append(" ").append(CTxT.of(name).color(color));
                }
                // fill in the gaps if entries don't fill whole page (consistency)
                if (listPage.getPage(pg).size() != PER_PAGE) {
                    for (int i = listPage.getPage(pg).size(); i < PER_PAGE; i++)
                        list.append("\n   ");
                }
            }
            // get the correct back button
            String backCMD = switch (type) {
                case hud -> "/hud color "+subtype+" edit "+UISettings;
                case dest -> "/dest settings "+subtype+" "+UISettings;
                case saved -> "/dest saved edit colorui \""+subtype+"\" "+UISettings;
                case preset -> "/dhud preset colorui \""+subtype+"\" "+UISettings;
                default -> "/dhud";
            };
            // final building of the message
            CTxT msg = CTxT.of(" ").append(lang("ui").color(Assets.mainColors.presets))
                    .append(CTxT.of("\n                               \n").strikethrough(true))
                    .append(" ").append(defaultBtn).append(" ").append(minecraftBtn).append("\n").append(list)
                    .append("\n\n   ").append(customBtn).append("  ").append(CUtl.CButton.back(backCMD))
                    .append(CTxT.of("\n                               ").strikethrough(true));
            player.sendMessage(msg);
        }
        /**
         * everything for the custom color presets
         */
        public static class custom {
            /**
             * update from the old preset system to the new one (1.7)
             * @param oldList the old list to update
             * @return the updated list
             */
            public static ArrayList<String> updateTo1_7(ArrayList<String> oldList) {
                ArrayList<String> list = new ArrayList<>();
                if (oldList.size() < 14) return list; // broken list return empty
                for (byte i = 0; i < 14; i++) {
                    String old = oldList.get(i);
                    if (!old.equals("#ffffff")) list.add((i+1)+"|"+old);
                }
                return list;
            }
            /**
             * validate the config entry, making sure it works without throwing errors
             * @param list the current preset list
             * @return the fixed (or validated list)
             */
            public static ArrayList<String> validate(ArrayList<String> list) {
                ArrayList<String> output = new ArrayList<>();
                for (String preset: list) {
                    String name = getName(preset), color = getColor(preset);
                    // if name too long, remove
                    if (name.length() > Helper.MAX_NAME) break;
                    // if not formatted properly, remove
                    if (!preset.contains("|#")) break;
                    // if color is invalid, remove
                    if (!color.equals("#ffffff") && CUtl.color.format(color).equals("#ffffff")) break;
                    output.add(preset);
                }
                return output;
            }
            /**
             * gets the color badge of the preset
             * @param preset the preset to make the badge
             * @param square if there should be a square with the color or not
             * @return the badge
             */
            public static CTxT getBadge(String preset, boolean square) {
                return CTxT.of((square?Assets.symbols.square+" ":"")+getName(preset)).color(getColor(preset));
            }
            /**
             * gets the name from the whole preset entry
             * @param preset the preset entry
             * @return the name of the preset
             */
            public static String getName(String preset) {
                return preset.substring(0, preset.lastIndexOf("|#"));
            }
            /**
             * gets the list of all preset names
             * @param presets the list with all presets
             * @return the list of all preset names
             */
            public static ArrayList<String> getNames(ArrayList<String> presets) {
                ArrayList<String> out = new ArrayList<>();
                for (String preset : presets) out.add(getName(preset));
                return out;
            }
            /**
             * gets the color from the whole preset entry
             * @param preset the preset entry
             * @return the color of the preset
             */
            public static String getColor(String preset) {
                return preset.substring(preset.lastIndexOf("|#")+1);
            }
            /**
             * gets the list of all preset colors
             * @param presets the list with all presets
             * @return the list of all preset colors
             */
            public static ArrayList<String> getColors(ArrayList<String> presets) {
                ArrayList<String> out = new ArrayList<>();
                for (String preset : presets) out.add(getColor(preset));
                return out;
            }
            /**
             * the custom presets UI
             * @param pg the page of the custom presets to display
             * @param aboveTxT the TxT to show above the UI
             */
            public static void UI(Player player, int pg, CTxT aboveTxT) {
                CTxT msg = aboveTxT==null?CTxT.of(" "):aboveTxT.append("\n "),
                        line = CTxT.of("\n                               ").strikethrough(true);
                msg.append(lang("ui.custom").color(Assets.mainColors.presets)).append(line);
                CTxT addBtn = CTxT.of("+").btn(true).color('a').cEvent(2,"/dhud preset save-r ").hEvent(lang("hover.save").color('a'));
                // disable if max saved colors reached
                if (PlayerData.get.colorPresets(player).size() >= config.MAXColorPresets) addBtn.color('7').cEvent(1,null).hEvent(null);
                ListPage<String> listPage = new ListPage<>(PlayerData.get.colorPresets(player),PER_PAGE);
                for (String preset : listPage.getPage(pg)) {
                    String color = getColor(preset), name = getName(preset);
                    msg.append("\n ").append(CTxT.of(Assets.symbols.x).color('c').btn(true)
                                    .cEvent(1,String.format("/dhud preset delete-r \"%s\"",name))
                                    .hEvent(lang("hover.delete",getBadge(preset,true)).color('c')))
                            .append(" ")
                            .append(CTxT.of(Assets.symbols.square).color(color).btn(true)
                                    .cEvent(1,String.format("/dhud preset colorui \"%s\" normal",name))
                                    .hEvent(lang("hover.color",CUtl.color.getBadge(color))))
                            .append(CTxT.of(name).color(color).btn(true)
                                    .cEvent(2,String.format("/dhud preset rename-r \"%s\" ",name))
                                    .hEvent(lang("hover.rename",getBadge(preset,false))));
                }
                // fill in the gaps if entries don't fill whole page (consistency)
                if (listPage.getPage(pg).size() != PER_PAGE) {
                    for (int i = listPage.getPage(pg).size(); i < PER_PAGE; i++)
                        msg.append("\n");
                }
                msg.append("\n\n ")
                        .append(addBtn).append(" ")
                        .append(listPage.getNavButtons(pg,"/dhud preset "))
                        .append(" ").append(CUtl.CButton.back("/dhud"))
                        .append(line);
                player.sendMessage(msg);
            }
            /**
             * the UI for changing a preset color
             * @param UISettings the ui settings
             * @param name the name of the preset to edit
             * @param aboveTxT the TxT above the UI
             */
            public static void colorUI(Player player, String UISettings, String name, CTxT aboveTxT) {
                ArrayList<String> presets = PlayerData.get.colorPresets(player);
                ArrayList<String> names = getNames(presets);
                if (!names.contains(name)) return;
                String currentColor = getColors(presets).get(names.indexOf(name));
                CTxT line = CTxT.of("\n                               ").strikethrough(true);
                CTxT msg = CTxT.of("");
                if (aboveTxT != null) msg.append(aboveTxT).append("\n");

                msg.append(" ").append(lang("ui.color").color(currentColor))
                        .append(line).append("\n")
                        .append(preset.colorEditor(currentColor,UISettings,Type.preset,name,"/dhud preset colorui \""+name+"\" %s"))
                        .append("\n\n           ").append(CUtl.CButton.back(String.format("/dhud preset \"%s\"",name))).append(line);
                player.sendMessage(msg);
            }
            /**
             * sets the color of the selected preset
             * @param UISettings the ui settings
             * @param name the name of the preset
             * @param color the new color to set to
             * @param Return whether to return to the UI or not
             */
            public static void setColor(Player player, String UISettings, String name, String color, boolean Return) {
                ArrayList<String> presets = PlayerData.get.colorPresets(player);
                ArrayList<String> names = getNames(presets);
                // remove the bad data
                if (!names.contains(name)) {
                    player.sendMessage(preset.error("dhud.preset"));
                    return;
                }
                // color fixer
                color = CUtl.color.colorHandler(player,color);
                // find the pos of the current preset and replace with the new color
                int index = names.indexOf(name);
                String oldPreset = presets.get(index), preset = name+"|"+color;
                presets.set(index,preset);
                PlayerData.set.colorPresets(player,presets);
                if (Return) colorUI(player,UISettings,name,null);
                else player.sendMessage(CUtl.tag().append(lang("msg.color",getBadge(oldPreset,false),CUtl.color.getBadge(color))));
            }
            /**
             * saves a new preset
             * @param Return displays the UI or not
             */
            public static void save(Player player, String name, String color, boolean Return) {
                ArrayList<String> presets = PlayerData.get.colorPresets(player);
                // errors
                if (getNames(presets).contains(name)) {
                    player.sendMessage(error("duplicate"));
                    return;
                }
                if (name.length() > Helper.MAX_NAME) {
                    player.sendMessage(CUtl.error("length",Helper.MAX_NAME));
                    return;
                }
                if (presets.size() >= config.MAXColorPresets) {
                    player.sendMessage(error("max"));
                    return;
                }
                // fix the color
                color = CUtl.color.colorHandler(player,color);
                // add & save the preset
                String entry = name+"|"+color;
                presets.add(entry);
                PlayerData.set.colorPresets(player,presets);
                // listPage for getting the page of the new entry when returning
                ListPage<String> listPage = new ListPage<>(presets,PER_PAGE);
                CTxT msg = CUtl.tag().append(lang("msg.save",getBadge(entry,true)));
                if (Return) UI(player,listPage.getPageOf(presets.get(presets.size()-1)),msg);
                else player.sendMessage(msg);
            }
            /**
             * renames an existing preset
             * @param Return displays the UI or not
             */
            public static void rename(Player player, String name, String newName, boolean Return) {
                ArrayList<String> presets = PlayerData.get.colorPresets(player);
                ArrayList<String> names = getNames(presets);
                // remove the bad data
                if (!names.contains(name)) {
                    player.sendMessage(error("invalid"));
                    return;
                }
                if (names.contains(newName)) {
                    player.sendMessage(error("duplicate"));
                    return;
                }
                if (newName.length() > Helper.MAX_NAME) {
                    player.sendMessage(CUtl.error("length",Helper.MAX_NAME));
                    return;
                }
                int index = names.indexOf(name);
                String preset = newName+"|"+getColors(presets).get(index);
                presets.set(index,preset);
                PlayerData.set.colorPresets(player,presets);
                // player formatting
                CTxT msg = CUtl.tag().append(lang("msg.rename",getBadge(name+"|"+getColors(presets).get(index),false),getBadge(preset,false)));
                ListPage<String> listPage = new ListPage<>(names,PER_PAGE);
                if (Return) UI(player,listPage.getPageOf(name),msg);
                else player.sendMessage(msg);
            }
            /**
             * deletes an existing preset
             * @param Return displays the UI or not
             */
            public static void delete(Player player, String name, boolean Return) {
                ArrayList<String> presets = PlayerData.get.colorPresets(player);
                ArrayList<String> names = getNames(presets);
                // remove the bad data
                if (!names.contains(name)) {
                    player.sendMessage(error("invalid"));
                    return;
                }
                String preset = presets.get(names.indexOf(name));
                // remove the preset
                presets.remove(preset);
                PlayerData.set.colorPresets(player,presets);
                // player formatting
                CTxT msg = CUtl.tag().append(lang("msg.delete",getBadge(preset,true)));
                ListPage<String> listPage = new ListPage<>(names,PER_PAGE);
                if (Return) UI(player,listPage.getPageOf(name),msg);
                else player.sendMessage(msg);
            }
        }
    }
    /**
     * the main directionHUD UI
     */
    public static void UI(Player player) {
        CTxT line = CTxT.of("\n                             ").strikethrough(true);
        CTxT msg = CTxT.of(" ")
                .append(CTxT.of("DirectionHUD").color(CUtl.p())
                        .hEvent(CTxT.of(DirectionHUD.VERSION+Assets.symbols.link).color(CUtl.s()))
                        .cEvent(3,"https://modrinth.com/mod/directionhud/changelog"))
                .append(line).append("\n ");
        // hud
        if (Utl.checkEnabled.hud(player)) msg.append(HUD.button()).append("  ");
        // dest
        if (Utl.checkEnabled.destination(player)) msg.append(CUtl.CButton.DHUD.dest());
        msg.append("\n\n ");
        // presets
        if (Utl.checkEnabled.customPresets(player)) msg.append(preset.BUTTON).append(" ");
        // inbox
        if (config.social) msg.append(inbox.BUTTON);
        // reload (if enabled)
        if (Utl.checkEnabled.reload(player)) msg.append("\n\n ").append(RELOAD_BUTTON);
        msg.append(line);
        player.sendMessage(msg);
    }
}
