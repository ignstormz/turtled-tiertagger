package com.kevin.tiertagger;

import com.google.gson.*;
import com.kevin.tiertagger.model.GameMode;
import com.kevin.tiertagger.model.PlayerInfo;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/** Snapshot of TurtleTiers's public API; never makes an HTTP request on a render thread. */
public final class TierCache {
    private static final List<GameMode> GAMEMODES = List.of(
            new GameMode("vanilla", "Vanilla"), new GameMode("uhc", "UHC"),
            new GameMode("pot", "Pot"), new GameMode("nethop", "Neth OP"),
            new GameMode("smp", "SMP"), new GameMode("sword", "Sword"),
            new GameMode("axe", "Axe"), new GameMode("mace", "Mace"),
            new GameMode("spearmace", "Spear Mace"), new GameMode("cart", "Cart"),
            new GameMode("creeper", "Creeper"), new GameMode("diamondsmp", "Diamond SMP"));
    private static volatile Map<UUID, PlayerInfo> byUuid = Map.of();
    private static volatile Map<String, PlayerInfo> byName = Map.of();
    private static volatile boolean loading;

    public static void init() {
        refresh();
        var executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "TurtleTiers refresh");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(TierCache::refresh, 5, 5, TimeUnit.MINUTES);
    }

    public static synchronized void refresh() {
        if (loading) return;
        loading = true;
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://turtletiers-api.onrender.com/api/players"))
                .timeout(Duration.ofSeconds(25)).header("Accept", "application/json").GET().build();
        TierTagger.getClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) throw new IllegalStateException("HTTP " + response.statusCode());
                    if (response.body().length() > 8_000_000) throw new IllegalStateException("Oversized response");
                    return parse(response.body());
                }).whenComplete((profiles, error) -> {
                    if (error != null) {
                        TierTagger.getLogger().warn("TurtleTiers refresh failed; retaining previous rankings", error);
                    } else {
                        Map<UUID, PlayerInfo> uuids = new HashMap<>();
                        Map<String, PlayerInfo> names = new HashMap<>();
                        for (PlayerInfo p : profiles) {
                            try { uuids.put(UUID.fromString(p.uuid()), p); } catch (IllegalArgumentException ignored) { }
                            names.put(p.name().toLowerCase(Locale.ROOT), p);
                        }
                        byUuid = Map.copyOf(uuids);
                        byName = Map.copyOf(names);
                        TierTagger.getLogger().info("Loaded {} TurtleTiers players", profiles.size());
                    }
                    loading = false;
                });
    }

    public static List<PlayerInfo> parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (!root.has("players") || !root.get("players").isJsonArray()) throw new IllegalArgumentException("Missing players");
        List<PlayerInfo> profiles = new ArrayList<>();
        for (JsonElement item : root.getAsJsonArray("players")) {
            if (!item.isJsonObject()) continue;
            JsonObject p = item.getAsJsonObject();
            if (!p.has("uuid") || !p.has("name") || !p.has("rankings") || !p.get("rankings").isJsonObject()) continue;
            Map<String, PlayerInfo.Ranking> ranks = new HashMap<>();
            for (var entry : p.getAsJsonObject("rankings").entrySet()) {
                if (!entry.getValue().isJsonPrimitive()) continue;
                String tier = entry.getValue().getAsString().toUpperCase(Locale.ROOT);
                if (!tier.matches("(HT|MT|LT)[1-5]")) continue;
                int position = switch (tier.substring(0, 2)) { case "HT" -> 0; case "MT" -> 1; default -> 2; };
                ranks.put(entry.getKey().toLowerCase(Locale.ROOT), new PlayerInfo.Ranking(tier.charAt(2) - '0', position, null, null, 0));
            }
            if (ranks.isEmpty()) continue;
            Map<String, Integer> elos = new HashMap<>();
            if (p.has("elos") && p.get("elos").isJsonObject()) {
                for (var entry : p.getAsJsonObject("elos").entrySet()) {
                    if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                        elos.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().getAsInt());
                    }
                }
            }
            int elo = p.has("overallElo") ? p.get("overallElo").getAsInt() : 0;
            profiles.add(new PlayerInfo(p.get("uuid").getAsString(), p.get("name").getAsString(), Map.copyOf(ranks), Map.copyOf(elos), elo, 0, List.of(), false));
        }
        profiles.sort(Comparator.comparingInt(PlayerInfo::points).reversed());
        List<PlayerInfo> ranked = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            PlayerInfo p = profiles.get(i);
            ranked.add(new PlayerInfo(p.uuid(), p.name(), p.rankings(), p.elos(), p.points(), i + 1, p.badges(), p.combatMaster()));
        }
        return ranked;
    }

    public static List<GameMode> getGamemodes() { return GAMEMODES; }
    public static Optional<Map<String, PlayerInfo.Ranking>> getPlayerRankings(UUID uuid) {
        return Optional.ofNullable(byUuid.get(uuid)).map(PlayerInfo::rankings);
    }
    public static CompletableFuture<PlayerInfo> searchPlayer(String query) {
        PlayerInfo p = byName.get(query.toLowerCase(Locale.ROOT));
        if (p == null) { try { p = byUuid.get(UUID.fromString(query)); } catch (IllegalArgumentException ignored) { } }
        return p == null ? CompletableFuture.failedFuture(new NoSuchElementException("Unknown player: " + query)) : CompletableFuture.completedFuture(p);
    }
    public static void clearCache() { refresh(); }
    public static GameMode findNextMode(GameMode current) {
        int index = GAMEMODES.indexOf(current);
        return GAMEMODES.get((index + 1) % GAMEMODES.size());
    }
    public static Optional<GameMode> findMode(String id) {
        return GAMEMODES.stream().filter(m -> m.id().equalsIgnoreCase(id)).findFirst();
    }
    public static GameMode findModeOrUgly(String id) { return findMode(id).orElseGet(() -> new GameMode(id, id)); }
    private TierCache() { }
}
