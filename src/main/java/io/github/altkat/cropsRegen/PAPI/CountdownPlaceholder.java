package io.github.altkat.cropsRegen.PAPI;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CountdownPlaceholder extends PlaceholderExpansion {

    private final long countdownTime;
    private long endTime;

    public CountdownPlaceholder(long countdownTime) {
        this.countdownTime = countdownTime;
        this.endTime = System.currentTimeMillis() + countdownTime;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cropsregen";
    }

    @Override
    public @NotNull String getAuthor() {
        return "StreetMelodeez";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public synchronized String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.equals("remaining_time")) {
            long remainingTime = endTime - System.currentTimeMillis();
            if (remainingTime <= 0) {
                resetTimer();
                return formatTime(countdownTime / 1000);
            }
            return formatTime(remainingTime / 1000);
        }
        return null;
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d", minutes, sec);
    }

    public synchronized void resetTimer() {
        this.endTime = System.currentTimeMillis() + countdownTime;
    }
}
