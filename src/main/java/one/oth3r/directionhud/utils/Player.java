package one.oth3r.directionhud.utils;

import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.UUID;

public class Player {
    private org.bukkit.entity.Player player;
    public Player() {}
    @Nullable
    public static Player of(org.bukkit.entity.Player player) {
        Player instance = new Player();
        instance.player = player;
        if (instance.player == null) return null;
        return instance;
    }
    @Nullable
    public static Player of(String identifier) {
        Player instance = new Player();
        if (identifier.contains("-")) instance.player = Bukkit.getPlayer(UUID.fromString(identifier));
        else instance.player = Bukkit.getPlayer(identifier);
        if (instance.player == null) return null;
        return instance;
    }
    public void performCommand(String cmd) {
        player.performCommand(cmd);
    }
    public void sendMessage(CTxT message) {
        player.spigot().sendMessage(message.b());
    }
    public void sendActionBar(CTxT message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message.b());
    }
    public String getName() {
        return player.getName();
    }
    public org.bukkit.entity.Player getPlayer() {
        return player;
    }
    public String getUUID() {
        return player.getUniqueId().toString();
    }
    public String getDimension() {
        return Utl.dim.format(player.getWorld().getName());
    }
    public float getYaw() {
        return player.getLocation().getYaw();
    }
    public Vector getVector() {
        return player.getLocation().toVector();
    }
    public int getBlockX() {
        return player.getLocation().getBlockX();
    }
    public int getBlockY() {
        return player.getLocation().getBlockY();
    }
    public int getBlockZ() {
        return player.getLocation().getBlockZ();
    }
    public void spawnParticleLine(Vector end, String particleType) {
        Vector pVec = player.getLocation().toVector().add(new Vector(0, 1, 0));
        if (player.getVehicle() != null) pVec.add(new Vector(0,-0.2,0));
        double distance = pVec.distance(end);
        Vector particlePos = pVec.subtract(new Vector(0, 0.2, 0));
        double spacing = 1;
        Vector segment = end.subtract(pVec).normalize().multiply(spacing);
        double distCovered = 0;
        for (; distCovered <= distance; particlePos = particlePos.add(segment)) {
            distCovered += spacing;
            if (pVec.distance(end) < 2) continue;
            if (distCovered >= 50) break;
            player.spawnParticle(Particle.REDSTONE,particlePos.getX(),particlePos.getY(),particlePos.getZ(),1,Utl.particle.getParticle(particleType, Player.of(player)));
        }
    }
}
