package dev.mattidragon.polydexbridge;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mattidragon.polydexbridge.data.BridgeCategory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CategoryCache {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("polydex_bridge_categories.json");

    public record SavedCategory(String id, String jsonName) {}

    public static Set<SavedCategory> load() {
        if (!Files.exists(PATH)) return Collections.emptySet();
        try (Reader reader = Files.newBufferedReader(PATH)) {
            Type type = new TypeToken<Set<SavedCategory>>() {}.getType();
            Set<SavedCategory> set = GSON.fromJson(reader, type);
            return set != null ? set : Collections.emptySet();
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    public static void save(Collection<BridgeCategory> categories) {
        Set<SavedCategory> current = new HashSet<>(load());
        for (BridgeCategory cat : categories) {
            current.add(new SavedCategory(cat.id().toString(), cat.name().getString()));
        }
        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(current, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}