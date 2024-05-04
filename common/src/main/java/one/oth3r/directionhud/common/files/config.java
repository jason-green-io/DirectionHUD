package one.oth3r.directionhud.common.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import one.oth3r.directionhud.DirectionHUD;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    private Float version = 1.6f;
    private String lang = "en_us";
    private Location location = new Location();
    private Boolean online = true;
    private Hud hud = new Hud();
    private Destination destination = new Destination();
    private Social social = new Social();
    private Integer maxColorPresets = 14;

    public Config() {}

    public Config(Config config) {
        this.version = config.version;
        this.lang = config.lang;
        this.location = new Location(config.location);
        this.online = config.online;
        this.hud = new Hud(config.hud);
        this.destination = new Destination(config.destination);
        this.social = new Social(config.social);
        this.maxColorPresets = config.maxColorPresets;
    }

    public Social getSocial() {
        return social;
    }

    public void setSocial(Social social) {
        this.social = social;
    }

    public Float getVersion() {
        return version;
    }

    public void setVersion(Float version) {
        this.version = version;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Hud getHud() {
        return hud;
    }

    public void setHud(Hud hud) {
        this.hud = hud;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Integer getMaxColorPresets() {
        return maxColorPresets;
    }

    public void setMaxColorPresets(Integer maxColorPresets) {
        this.maxColorPresets = maxColorPresets;
    }

    public static class Social {
        private Boolean enabled = true;
        private Integer cooldown = 10;

        public Social() {}

        public Social(Social social) {
            this.enabled = social.enabled;
            this.cooldown = social.cooldown;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getCooldown() {
            return cooldown;
        }

        public void setCooldown(Integer cooldown) {
            this.cooldown = cooldown;
        }
    }

    public static class Hud {
        private Boolean editing = true;
        private Integer loop = 1;

        public Hud() {}

        public Hud(Hud hud) {
            this.editing = hud.editing;
            this.loop = hud.loop;
        }

        public Boolean getEditing() {
            return editing;
        }

        public void setEditing(Boolean editing) {
            this.editing = editing;
        }

        public Integer getLoop() {
            return loop;
        }

        public void setLoop(Integer loop) {
            this.loop = loop;
        }
    }

    public static class Destination {
        private Boolean saving = true;
        private Integer maxSaved = 50;
        private Boolean gloabal = true;
        private LastDeath lastDeath = new LastDeath();
        private Integer particleLoop = 20;

        public Destination() {}

        public Destination(Destination destination) {
            this.saving = destination.saving;
            this.maxSaved = destination.maxSaved;
            this.gloabal = destination.gloabal;
            this.lastDeath = new LastDeath(destination.lastDeath);
            this.particleLoop = destination.particleLoop;
        }

        public Boolean getSaving() {
            return saving;
        }

        public void setSaving(Boolean saving) {
            this.saving = saving;
        }

        public Integer getMaxSaved() {
            return maxSaved;
        }

        public void setMaxSaved(Integer maxSaved) {
            this.maxSaved = maxSaved;
        }

        public Boolean getGloabal() {
            return gloabal;
        }

        public void setGloabal(Boolean gloabal) {
            this.gloabal = gloabal;
        }

        public LastDeath getLastDeath() {
            return lastDeath;
        }

        public void setLastDeath(LastDeath lastDeath) {
            this.lastDeath = lastDeath;
        }

        public Integer getParticleLoop() {
            return particleLoop;
        }

        public void setParticleLoop(Integer particleLoop) {
            this.particleLoop = particleLoop;
        }

        public static class LastDeath {
            private Boolean saving = true;
            private Integer maxDeaths = 4;

            public LastDeath() {
            }

            public LastDeath(LastDeath lastDeath) {
                this.saving = lastDeath.saving;
                this.maxDeaths = lastDeath.maxDeaths;
            }

            public Boolean getSaving() {
                return saving;
            }

            public void setSaving(Boolean saving) {
                this.saving = saving;
            }

            public Integer getMaxDeaths() {
                return maxDeaths;
            }

            public void setMaxDeaths(Integer maxDeaths) {
                this.maxDeaths = maxDeaths;
            }
        }
    }

    public static class Location {
        private int maxY = 512;
        private int maxXZ = 30000000;

        public Location(Location location) {
            this.maxY = location.maxY;
            this.maxXZ = location.maxXZ;
        }

        public Location() {
        }

        public int getMaxY() {
            return maxY;
        }

        public void setMaxY(int maxY) {
            this.maxY = maxY;
        }

        public int getMaxXZ() {
            return maxXZ;
        }

        public void setMaxXZ(int maxXZ) {
            this.maxXZ = maxXZ;
        }
    }

    public static File getFile() {
        return new File(DirectionHUD.CONFIG_DIR+"config.json");
    }

    /**
     * loads the directionhud Config file to Data.config
     */
    public static void load(boolean tryLegacy) {

        File file = getFile();
        if (!file.exists()) {
            // try to make the config directory
            try {
                Files.createDirectories(Paths.get(DirectionHUD.CONFIG_DIR));
            } catch (Exception e) {
                DirectionHUD.LOGGER.info("Failed to create config directory. Canceling all config loading...");
                return;
            }
            // if loading from legacy, try checking the old config directory for the file
            if (tryLegacy && Updater.ConfigFile.Legacy.getLegacyFile().exists()) {
                DirectionHUD.LOGGER.info("Updating DirectionHUD.properties to directionhud/config.json");
                Updater.ConfigFile.Legacy.run();
            }
            DirectionHUD.LOGGER.info(String.format("Creating new `%s`", file.getName()));
            save();
        }
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            Gson gson = new GsonBuilder().create();
            Data.setConfig(gson.fromJson(reader, Config.class));
        } catch (Exception e) {
            DirectionHUD.LOGGER.info(String.format("ERROR LOADING '%s`", file.getName()));
            e.printStackTrace();
        }
        save();
    }

    /**
     * saves Data.config to config.json
     */
    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(getFile().toPath())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(Data.getConfig()));
        } catch (Exception e) {
            DirectionHUD.LOGGER.info(String.format("ERROR SAVING '%s`",getFile().getName()));
            e.printStackTrace();
        }
    }
}
