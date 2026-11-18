package com.gtocore.integration.emi.jer;

import com.gtolib.GTOCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.ForgeSpawnEggItem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// 改写自 Just Enough Resources（way2muchnoise，"Don't Be a Jerk" 非商业许可）
// https://github.com/way2muchnoise/JustEnoughResources
public final class JerMobRecipe implements EmiRecipe {

    private static final int WIDTH = 160;
    private static final int GRID_X = 88;
    private static final int GRID_Y = 42;
    private static final int COLUMNS = 4;
    private static final int MIN_ROWS = 4;
    private static final int VIEW_W = 80;
    private static final int VIEW_H = MIN_ROWS * 18;
    private static final int VIEW_PAD = 4;
    private static final int MAX_SCALE = 40;

    private int scale;
    private int originX;
    private int originY;
    private int headY;

    private final ResourceLocation id;
    private final ResourceLocation table;
    private final EntityType<?> type;
    private final @Nullable DyeColor color;
    private final List<JerLoot.Drop> drops;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;
    private WeakReference<LivingEntity> entity = new WeakReference<>(null);

    private JerMobRecipe(ResourceLocation table, EntityType<?> type, @Nullable DyeColor color, List<JerLoot.Drop> drops) {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        this.id = GTOCore.id("/jer/mob/" + typeId.getNamespace() + "/" + typeId.getPath() + (color == null ? "" : "/" + color.getName()));
        this.table = table;
        this.type = type;
        this.color = color;
        this.drops = drops;
        List<EmiStack> out = new ArrayList<>(drops.size());
        for (JerLoot.Drop drop : drops) out.add(EmiStack.of(drop.stack()));
        this.outputs = out;
        SpawnEggItem egg = ForgeSpawnEggItem.fromEntityType(type);
        this.catalysts = egg == null ? Collections.emptyList() : Collections.singletonList(EmiStack.of(egg));
    }

    static List<JerMobRecipe> create(JerLootSync.Dataset data) {
        List<JerMobRecipe> recipes = new ArrayList<>();
        ClientLevel level = Minecraft.getInstance().level;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() == MobCategory.MISC) continue;
            if (level != null && !type.isEnabled(level.enabledFeatures())) continue;
            if (type == EntityType.SHEEP) {
                for (DyeColor color : DyeColor.values()) {
                    ResourceLocation table = sheepTable(color);
                    recipes.add(new JerMobRecipe(table, type, color, distinct(data.mobDrops(table))));
                }
            } else {
                ResourceLocation table = type.getDefaultLootTable();
                recipes.add(new JerMobRecipe(table, type, null, distinct(data.mobDrops(table))));
            }
        }
        recipes.sort(Comparator.comparing(r -> r.table));
        return recipes;
    }

    private static List<JerLoot.Drop> distinct(List<JerLoot.Drop> drops) {
        ReferenceOpenHashSet<Item> seen = new ReferenceOpenHashSet<>(drops.size());
        List<JerLoot.Drop> result = new ArrayList<>(drops.size());
        for (JerLoot.Drop drop : drops) {
            if (seen.add(drop.stack().getItem())) result.add(drop);
        }
        return result;
    }

    private static ResourceLocation sheepTable(DyeColor color) {
        return switch (color) {
            case WHITE -> BuiltInLootTables.SHEEP_WHITE;
            case ORANGE -> BuiltInLootTables.SHEEP_ORANGE;
            case MAGENTA -> BuiltInLootTables.SHEEP_MAGENTA;
            case LIGHT_BLUE -> BuiltInLootTables.SHEEP_LIGHT_BLUE;
            case YELLOW -> BuiltInLootTables.SHEEP_YELLOW;
            case LIME -> BuiltInLootTables.SHEEP_LIME;
            case PINK -> BuiltInLootTables.SHEEP_PINK;
            case GRAY -> BuiltInLootTables.SHEEP_GRAY;
            case LIGHT_GRAY -> BuiltInLootTables.SHEEP_LIGHT_GRAY;
            case CYAN -> BuiltInLootTables.SHEEP_CYAN;
            case PURPLE -> BuiltInLootTables.SHEEP_PURPLE;
            case BLUE -> BuiltInLootTables.SHEEP_BLUE;
            case BROWN -> BuiltInLootTables.SHEEP_BROWN;
            case GREEN -> BuiltInLootTables.SHEEP_GREEN;
            case RED -> BuiltInLootTables.SHEEP_RED;
            case BLACK -> BuiltInLootTables.SHEEP_BLACK;
        };
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return JerEmi.MOB;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        int rows = Math.max(MIN_ROWS, (drops.size() + COLUMNS - 1) / COLUMNS);
        return GRID_Y + rows * 18 + 2;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        LivingEntity living = entity();
        Component name = color == null ? type.getDescription() :
                Component.translatable(JerEmi.Lang.SHEEP_NAME, type.getDescription(), Component.translatable(colorKey(color)));
        widgets.addText(name, 2, 2, 0x404040, false);
        widgets.addText(Component.translatable(JerEmi.Lang.BIOME, Component.translatable(JerEmi.Lang.ANY)), 2, 12, 0x404040, false);
        widgets.addText(Component.translatable(JerEmi.Lang.LIGHT, lightLevel(living)), 2, 22, 0x404040, false);
        widgets.addText(Component.translatable(JerEmi.Lang.EXP, experience(living)), 2, 32, 0x404040, false);
        if (!catalysts.isEmpty()) widgets.addSlot(catalysts.get(0), WIDTH - 18, 18).catalyst(true);
        if (living != null) {
            LivingEntity[] held = { living };
            widgets.addDrawable(0, GRID_Y, VIEW_W, VIEW_H, (g, mouseX, mouseY, delta) -> {
                LivingEntity e = held[0];
                if (e.level() != Minecraft.getInstance().level) {
                    e = entity();
                    if (e == null) return;
                    held[0] = e;
                }
                if (scale == 0) fit(e);
                Matrix4f pose = g.pose().last().pose();
                int left = Math.round(pose.m30());
                int top = Math.round(pose.m31());
                g.enableScissor(left, top, left + VIEW_W, top + VIEW_H);
                try {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(g, originX, originY, scale, originX - mouseX, GRID_Y + headY - mouseY, e);
                } finally {
                    g.disableScissor();
                }
            });
        }
        for (int i = 0; i < drops.size(); i++) {
            JerLoot.Drop drop = drops.get(i);
            JerEmi.decorate(widgets.addSlot(outputs.get(i), GRID_X + (i % COLUMNS) * 18, GRID_Y + (i / COLUMNS) * 18).recipeContext(this), drop);
        }
    }

    private @Nullable LivingEntity entity() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        LivingEntity cached = entity.get();
        if (cached != null && cached.level() == level) return cached;
        LivingEntity created = null;
        try {
            Entity e = type.create(level);
            if (e instanceof LivingEntity living) created = living;
        } catch (RuntimeException ignored) {}
        if (created instanceof Sheep sheep && color != null) sheep.setColor(color);
        entity = new WeakReference<>(created);
        return created;
    }

    private static Component lightLevel(@Nullable LivingEntity entity) {
        int below = -1;
        if (entity instanceof Mob && !(entity instanceof Vex || entity instanceof Guardian || (entity instanceof PatrollingMonster && !(entity instanceof Witch)))) {
            if (entity instanceof Blaze) below = 12;
            else if (entity instanceof Monster || entity instanceof Slime || entity instanceof Phantom) below = 8;
            else if (entity instanceof Bat) below = 4;
        }
        return below < 0 ? Component.translatable(JerEmi.Lang.ANY) : Component.translatable(JerEmi.Lang.LIGHT_BELOW, below);
    }

    private static String experience(@Nullable LivingEntity entity) {
        if (!(entity instanceof Mob mob)) return "0";
        if (mob instanceof Animal || mob instanceof WaterAnimal) return "1 - 3";
        if (mob instanceof EnderDragon) return "500 - 12000";
        if (mob instanceof Slime || mob instanceof MagmaCube) return "1 - 4";
        try {
            return Integer.toString(mob.getExperienceReward());
        } catch (RuntimeException e) {
            return "0";
        }
    }

    private void fit(LivingEntity entity) {
        Extent extent = new Extent();
        if (!extent.measure(entity)) extent.box(entity.getBbWidth(), entity.getBbHeight());
        float width = Math.max(extent.maxX - extent.minX, 0.1F);
        float height = Math.max(extent.maxY - extent.minY, 0.1F);
        float fit = Math.min((VIEW_W - 2 * VIEW_PAD) / width, (VIEW_H - 2 * VIEW_PAD) / height);
        scale = Math.max(1, Math.min(MAX_SCALE, (int) fit));
        originX = Math.round(VIEW_W / 2F - (extent.minX + extent.maxX) / 2 * scale);
        originY = VIEW_H - VIEW_PAD - Math.round(extent.maxY * scale);
        headY = originY + Math.round(extent.minY * scale * 0.85F);
    }

    private static final class Extent implements VertexConsumer, MultiBufferSource {

        private static final float REACH = (float) (Math.PI / 2);
        private static final float[][] POSES = { { 0, 0 }, { REACH, 0 }, { -REACH, 0 }, { 0, REACH }, { 0, -REACH } };

        private float minX = Float.MAX_VALUE;
        private float maxX = -Float.MAX_VALUE;
        private float minY = Float.MAX_VALUE;
        private float maxY = -Float.MAX_VALUE;

        private boolean measure(LivingEntity entity) {
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            float bodyRot = entity.yBodyRot;
            float yRot = entity.getYRot();
            float xRot = entity.getXRot();
            float headRot = entity.yHeadRot;
            float headRotO = entity.yHeadRotO;
            dispatcher.setRenderShadow(false);
            try {
                for (float[] pose : POSES) render(dispatcher, entity, pose[0], pose[1]);
            } catch (RuntimeException e) {
                return false;
            } finally {
                dispatcher.setRenderShadow(true);
                entity.yBodyRot = bodyRot;
                entity.setYRot(yRot);
                entity.setXRot(xRot);
                entity.yHeadRot = headRot;
                entity.yHeadRotO = headRotO;
            }
            return maxX > minX && maxY > minY;
        }

        private void render(EntityRenderDispatcher dispatcher, LivingEntity entity, float yaw, float pitch) {
            Quaternionf tilt = new Quaternionf().rotateX(pitch * 20F * ((float) Math.PI / 180F));
            PoseStack pose = new PoseStack();
            pose.mulPoseMatrix(new Matrix4f().scaling(1, 1, -1));
            pose.mulPose(new Quaternionf().rotateZ((float) Math.PI).mul(tilt));
            entity.yBodyRot = 180F + yaw * 20F;
            entity.setYRot(180F + yaw * 40F);
            entity.setXRot(-pitch * 20F);
            entity.yHeadRot = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            dispatcher.overrideCameraOrientation(tilt.conjugate());
            dispatcher.render(entity, 0, 0, 0, 0F, 1F, pose, this, LightTexture.FULL_BRIGHT);
        }

        private void box(float width, float height) {
            minX = -width / 2;
            maxX = width / 2;
            minY = -height;
            maxY = 0;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return this;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            float fx = (float) x;
            float fy = (float) y;
            if (fx < minX) minX = fx;
            if (fx > maxX) maxX = fx;
            if (fy < minY) minY = fy;
            if (fy > maxY) maxY = fy;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void endVertex() {}

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {}

        @Override
        public void unsetDefaultColor() {}
    }

    private static String colorKey(DyeColor color) {
        return switch (color) {
            case WHITE -> "color.minecraft.white";
            case ORANGE -> "color.minecraft.orange";
            case MAGENTA -> "color.minecraft.magenta";
            case LIGHT_BLUE -> "color.minecraft.light_blue";
            case YELLOW -> "color.minecraft.yellow";
            case LIME -> "color.minecraft.lime";
            case PINK -> "color.minecraft.pink";
            case GRAY -> "color.minecraft.gray";
            case LIGHT_GRAY -> "color.minecraft.light_gray";
            case CYAN -> "color.minecraft.cyan";
            case PURPLE -> "color.minecraft.purple";
            case BLUE -> "color.minecraft.blue";
            case BROWN -> "color.minecraft.brown";
            case GREEN -> "color.minecraft.green";
            case RED -> "color.minecraft.red";
            case BLACK -> "color.minecraft.black";
        };
    }
}
