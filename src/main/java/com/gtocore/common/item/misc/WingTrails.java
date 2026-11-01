package com.gtocore.common.item.misc;

import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.player.OrganInventory;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.suried.gtorenders.effect.BodyEffects;
import com.suried.gtorenders.effect.MoteLook;
import com.suried.gtorenders.effect.MoteParticles;
import com.suried.gtorenders.effect.PixelSheet;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.WeakHashMap;

/**
 * 翅膀特效：服务端在穿戴的翅膀、能否使用、是否飞行变化时同步给同维度玩家；客户端用 GTORenders 身体挂件把像素光片翅膀挂在后肩，
 * 飞行时展开发光并飘落光点，其余时候收拢、下垂、暗沉，两种姿态平滑过渡。
 */
public final class WingTrails {

    public static final Style FAIRY = new Style(Shape.FAIRY, 0xE0409E, 0x40C8F0, 0xFF9CE4, 0xFFF06A, 0.5F, 0.45F);
    public static final Style MANA_STEEL = new Style(Shape.FEATHER, 0x1440C8, 0x2AA6F0, 0x8CE4FF, 0x56D8FF, 0.3F, 0.35F);
    public static final Style MECHANICAL = new Style(Shape.BLADE, 0x283C90, 0x3C78C8, 0x9AD8FF, 0xFFB040, 0.08F, 0.06F);

    private static final float PIXEL = 0.065F;
    private static final float SPAN = 1.45F;
    private static final float V_MIN = -1.1F, V_MAX = 1.2F;

    private static final NetworkPack SYNC = NetworkPack.registerS2C("gtocoreWingTrails", (player, buf) -> Client.receive(buf));
    private static final WeakHashMap<ServerPlayer, Integer> SHOWN = new WeakHashMap<>();
    private static final int FLYING = 1, USABLE = 2, WEAR_SHIFT = 2, ITEM_SHIFT = 4;

    private WingTrails() {}

    public static final class Style {

        private final float flapSpeed, flapAngle;
        private final float[] x, y, alpha;
        private final int[] rgb;

        public Style(Shape shape, int inner, int outer, int edge, int glow, float flapSpeed, float flapAngle) {
            this.flapSpeed = flapSpeed;
            this.flapAngle = flapAngle;
            int columns = Math.round(SPAN / PIXEL);
            int rowMin = Mth.floor(V_MIN / PIXEL), rows = Mth.ceil(V_MAX / PIXEL) - rowMin;
            var grid = new boolean[columns][rows];
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    grid[c][r] = shape.inside((c + 0.5F) * PIXEL / SPAN, (r + rowMin + 0.5F) * PIXEL);
                }
            }
            var xs = new FloatArrayList();
            var ys = new FloatArrayList();
            var alphas = new FloatArrayList();
            var colors = new IntArrayList();
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    if (!grid[c][r]) continue;
                    float u = (c + 0.5F) * PIXEL / SPAN, v = (r + rowMin + 0.5F) * PIXEL;
                    boolean border = c > 0 && (c + 1 == columns || !grid[c + 1][r] || !grid[c - 1][r] || r + 1 == rows || !grid[c][r + 1] || r == 0 || !grid[c][r - 1]);
                    int base = lerpColor(inner, outer, Math.min(1, Mth.sqrt(u * u + v * v * 0.64F)));
                    int accent = shape.accent(u, v);
                    int color;
                    float a;
                    if (border) {
                        color = edge;
                        a = 0.6F;
                    } else if (accent == 2) {
                        color = glow;
                        a = 0.5F;
                    } else if (accent == 1) {
                        color = lerpColor(base, edge, 0.5F);
                        a = 0.4F;
                    } else {
                        color = base;
                        a = 0.3F;
                    }
                    xs.add(u * SPAN);
                    ys.add(v);
                    alphas.add(a);
                    colors.add(color);
                }
            }
            this.x = xs.toFloatArray();
            this.y = ys.toFloatArray();
            this.alpha = alphas.toFloatArray();
            this.rgb = colors.toIntArray();
        }
    }

    public enum Shape {

        FAIRY {

            @Override
            boolean inside(float u, float v) {
                return ellipse(u, v, 0.55F, 0.42F, 0.5F, 0.36F, 0.5F) || ellipse(u, v, 0.36F, -0.28F, 0.34F, 0.26F, -0.45F);
            }

            @Override
            int accent(float u, float v) {
                if (ellipse(u, v, 0.68F, 0.55F, 0.13F, 0.11F, 0.5F) || ellipse(u, v, 0.42F, -0.32F, 0.09F, 0.08F, 0)) return 2;
                float angle = (float) Mth.atan2(v, u);
                float width = 0.056F / Math.max(0.15F, Mth.sqrt(u * u + v * v));
                for (float ray : VEINS) {
                    if (Math.abs(angle - ray) < width) return 1;
                }
                return 0;
            }
        },
        FEATHER {

            @Override
            boolean inside(float u, float v) {
                float top = lead(u);
                if (u > 1 || v > top) return false;
                float k = u * FEATHERS, f = k - Mth.floor(k);
                float bottom = top - featherLength(u) - 0.1375F * Math.abs(Mth.sin(f * Mth.PI)) + 0.12F;
                return v >= bottom && (f >= 0.14F || v >= top - 0.45F);
            }

            @Override
            int accent(float u, float v) {
                float top = lead(u);
                if (v > top - 0.2F) return 2;
                float k = u * FEATHERS, f = k - Mth.floor(k);
                return f > 0.5F && f < 0.64F && v < top - 0.45F ? 1 : 0;
            }
        },
        BLADE {

            @Override
            boolean inside(float u, float v) {
                return blade(u, v) >= 0;
            }

            @Override
            int accent(float u, float v) {
                int i = blade(u, v);
                if (i < 0) return 0;
                float angle = BLADES[i * 3], length = BLADES[i * 3 + 1], offset = BLADES[i * 3 + 2];
                float cos = Mth.cos(angle), sin = Mth.sin(angle);
                float along = u * cos + (v - offset) * sin, across = -u * sin + (v - offset) * cos;
                if (along > length - 0.16F) return 2;
                return Math.abs(across) < 0.025F || (int) (along / 0.22F) != (int) ((along + 0.06F) / 0.22F) ? 1 : 0;
            }
        };

        private static final float[] VEINS = { -0.75F, -0.3F, 0.25F, 0.6F, 0.95F };
        private static final int FEATHERS = 7;
        private static final float[] BLADES = { 0.62F, 1.05F, 0, 0.25F, 0.9F, -0.3F, -0.12F, 0.72F, -0.58F };

        abstract boolean inside(float u, float v);

        abstract int accent(float u, float v);

        private static boolean ellipse(float u, float v, float cu, float cv, float ru, float rv, float angle) {
            float du = u - cu, dv = v - cv, cos = Mth.cos(angle), sin = Mth.sin(angle);
            float x = (du * cos + dv * sin) / ru, y = (dv * cos - du * sin) / rv;
            return x * x + y * y <= 1;
        }

        private static float lead(float u) {
            return 0.1F + 0.85F * (1 - (1 - u) * (1 - u));
        }

        private static float featherLength(float u) {
            float length = 0.3F + 1.5F * u * Mth.sqrt(1 - u);
            return u > 0.88F ? length * Math.max(0, 1 - (u - 0.88F) / 0.12F * 0.75F) : length;
        }

        private static int blade(float u, float v) {
            for (int i = 0; i < BLADES.length / 3; i++) {
                float angle = BLADES[i * 3], length = BLADES[i * 3 + 1], offset = BLADES[i * 3 + 2];
                float cos = Mth.cos(angle), sin = Mth.sin(angle);
                float along = u * cos + (v - offset) * sin, across = -u * sin + (v - offset) * cos;
                if (along >= 0 && along <= length && across >= -0.13F && across <= 0.14F && along + across * 1.2F <= length) return i;
            }
            return -1;
        }
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(WingTrails::onLogin);
        MinecraftForge.EVENT_BUS.addListener(WingTrails::onChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(WingTrails::onRespawn);
    }

    public static void update(ServerPlayer player, OrganInventory organs, @Nullable WingOrganItem active) {
        ItemStack worn = ItemStack.EMPTY;
        for (int i = 0; i < OrganType.WING.slotCount; i++) {
            var stack = organs.getStack(OrganType.WING, i);
            if (!(stack.getItem() instanceof WingOrganItem)) continue;
            if (active == null || stack.getItem() == active) {
                worn = stack;
                break;
            }
        }
        if (!(worn.getItem() instanceof WingOrganItem wing)) {
            sync(player, 0);
            return;
        }
        float remaining = wing.remaining(worn);
        int wear = remaining > 2 / 3F ? 0 : remaining > 1 / 3F ? 1 : 2;
        sync(player, BuiltInRegistries.ITEM.getId(wing) << ITEM_SHIFT | wear << WEAR_SHIFT | (active != null ? USABLE : 0) |
                (player.getAbilities().flying ? FLYING : 0));
    }

    public static void onAbilities(ServerPlayer player) {
        var state = SHOWN.get(player);
        if (state != null && state != 0) sync(player, state & ~FLYING | (player.getAbilities().flying ? FLYING : 0));
    }

    private static void sync(ServerPlayer player, int state) {
        var old = SHOWN.put(player, state);
        if (old != null && old == state) return;
        SYNC.send(out -> {
            out.writeVarInt(1);
            out.writeVarInt(player.getId());
            out.writeVarInt(state);
        }, player.serverLevel().players());
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) enterLevel(player);
    }

    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) enterLevel(player);
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) enterLevel(player);
    }

    private static void enterLevel(ServerPlayer player) {
        SHOWN.remove(player);
        List<ServerPlayer> players = player.serverLevel().players();
        int count = 0;
        for (int i = 0, size = players.size(); i < size; i++) {
            var state = SHOWN.get(players.get(i));
            if (state != null && state != 0) count++;
        }
        if (count == 0) return;
        int total = count;
        SYNC.send(out -> {
            out.writeVarInt(total);
            for (int i = 0, size = players.size(); i < size; i++) {
                var other = players.get(i);
                var state = SHOWN.get(other);
                if (state == null || state == 0) continue;
                out.writeVarInt(other.getId());
                out.writeVarInt(state);
            }
        }, player);
    }

    private static int lerpColor(int from, int to, float t) {
        int r = (int) Mth.lerp(t, from >> 16 & 0xFF, to >> 16 & 0xFF);
        int g = (int) Mth.lerp(t, from >> 8 & 0xFF, to >> 8 & 0xFF);
        int b = (int) Mth.lerp(t, from & 0xFF, to & 0xFF);
        return r << 16 | g << 8 | b;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Client {

        private static final Int2IntOpenHashMap STATES = new Int2IntOpenHashMap();
        private static final Int2FloatOpenHashMap LIFT = new Int2FloatOpenHashMap();
        private static final Int2FloatOpenHashMap SEEN = new Int2FloatOpenHashMap();
        private static final Int2LongOpenHashMap TRAIL_TICKS = new Int2LongOpenHashMap();
        private static final Reference2ObjectOpenHashMap<Style, PixelSheet[]> SHEETS = new Reference2ObjectOpenHashMap<>();
        private static final float[] WEAR_CUT = { Float.MAX_VALUE, 0.84F, 0.7F };
        private static final float[] WEAR_FADE = { 1, 0.9F, 0.75F };
        private static final float SINGE = 0.07F, SINGE_DIM = 0.35F, NOISE_CELL = 0.14F;
        private static final MoteLook LOOK = MoteLook.glow(0.45F);
        private static final float HALO = 1.6F, CORE = 0.45F, TRAIL_LIFE = 14;
        private static final float ROOT_DOWN = 0.3F, ROOT_BACK = 0.15F, ROOT_SIDE = 0.05F, MIN_FOLD = 0.15F;
        private static final float LIFT_PER_TICK = 0.08F, IDLE_FOLD = 1.05F, IDLE_DROOP = 0.55F, IDLE_SCALE = 0.82F;
        private static final float DIM_RED = 0.26F, DIM_GREEN = 0.28F, DIM_BLUE = 0.36F;
        private static final Vector3d WORLD = new Vector3d();

        private Client() {}

        public static void init() {
            MinecraftForge.EVENT_BUS.addListener(Client::onLoggingOut);
            BodyEffects.register(Client::render);
        }

        private static void receive(FriendlyByteBuf buf) {
            for (int i = buf.readVarInt(); i > 0; i--) {
                int entity = buf.readVarInt(), state = buf.readVarInt();
                if (state == 0) STATES.remove(entity);
                else STATES.put(entity, state);
            }
        }

        private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            STATES.clear();
            LIFT.clear();
            SEEN.clear();
            TRAIL_TICKS.clear();
        }

        private static PixelSheet sheet(Style style, int wear) {
            var sheets = SHEETS.get(style);
            if (sheets == null) {
                sheets = new PixelSheet[WEAR_CUT.length];
                SHEETS.put(style, sheets);
            }
            if (sheets[wear] != null) return sheets[wear];
            var builder = PixelSheet.builder(PIXEL, HALO, CORE);
            for (int p = 0; p < style.x.length; p++) {
                float x = style.x[p], y = style.y[p];
                float score = wearScore(x, y);
                if (score > WEAR_CUT[wear]) continue;
                float dim = WEAR_FADE[wear] * (score > WEAR_CUT[wear] - SINGE ? SINGE_DIM : 1);
                builder.pixel(x, y, scaleColor(style.rgb[p], dim), style.alpha[p]);
            }
            return sheets[wear] = builder.build();
        }

        private static float wearScore(float x, float y) {
            return 0.55F * noise(x / NOISE_CELL, y / NOISE_CELL) + 0.45F * x / SPAN + 0.12F * hash(Math.round(x / PIXEL) + 7, Math.round(y / PIXEL) + 13);
        }

        private static float noise(float x, float y) {
            int i = Mth.floor(x), j = Mth.floor(y);
            float tx = x - i, ty = y - j;
            tx = tx * tx * (3 - 2 * tx);
            ty = ty * ty * (3 - 2 * ty);
            return Mth.lerp(ty, Mth.lerp(tx, hash(i, j), hash(i + 1, j)), Mth.lerp(tx, hash(i, j + 1), hash(i + 1, j + 1)));
        }

        private static float hash(int i, int j) {
            double v = Math.sin(i * 127.1 + j * 311.7) * 43758.5453;
            return (float) (v - Math.floor(v));
        }

        private static int scaleColor(int rgb, float factor) {
            return (int) ((rgb >> 16 & 0xFF) * factor) << 16 | (int) ((rgb >> 8 & 0xFF) * factor) << 8 | (int) ((rgb & 0xFF) * factor);
        }

        private static float lift(int id, boolean active, float ageInTicks) {
            float target = active ? 1 : 0;
            if (!SEEN.containsKey(id)) {
                SEEN.put(id, ageInTicks);
                LIFT.put(id, target);
                return target;
            }
            float step = Mth.clamp(ageInTicks - SEEN.put(id, ageInTicks), 0, 5) * LIFT_PER_TICK;
            float lift = LIFT.get(id);
            lift = lift < target ? Math.min(target, lift + step) : Math.max(target, lift - step);
            LIFT.put(id, lift);
            return lift;
        }

        private static void render(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, PoseStack poseStack, float partialTick, float ageInTicks) {
            int id = player.getId();
            int state = STATES.get(id);
            if (state == 0 || player.isSpectator() || player.isInvisible() || !(BuiltInRegistries.ITEM.byId(state >> ITEM_SHIFT) instanceof WingOrganItem wing)) return;
            boolean flying = player.isLocalPlayer() ? player.getAbilities().flying : (state & FLYING) != 0;
            boolean active = (state & USABLE) != 0 && flying && !player.isFallFlying() && !player.isPassenger();
            float lift = lift(id, active, ageInTicks);
            var style = wing.getTrail();
            var sheet = sheet(style, Math.min(2, state >> WEAR_SHIFT & 3));
            float flap = style.flapAngle * (1 + Mth.sin((ageInTicks + id) * style.flapSpeed % Mth.TWO_PI));
            float breath = 0.04F * Mth.sin((ageInTicks + id) * 0.06F % Mth.TWO_PI);
            float fold = Mth.lerp(lift, IDLE_FOLD + breath, MIN_FOLD + flap);
            float droop = (1 - lift) * IDLE_DROOP;
            float scale = Mth.lerp(lift, IDLE_SCALE, 1);
            float red = Mth.lerp(lift, DIM_RED, 1), green = Mth.lerp(lift, DIM_GREEN, 1), blue = Mth.lerp(lift, DIM_BLUE, 1);
            long tick = player.level().getGameTime();
            boolean trail = lift > 0.85F && TRAIL_TICKS.put(id, tick) != tick;
            var random = player.getRandom();
            poseStack.pushPose();
            model.body.translateAndRotate(poseStack);
            poseStack.translate(0, ROOT_DOWN + (1 - lift) * 0.04F, ROOT_BACK);
            for (int side = -1; side <= 1; side += 2) {
                poseStack.pushPose();
                poseStack.translate(side * ROOT_SIDE, 0, 0);
                poseStack.mulPose(Axis.ZP.rotation(side * droop));
                poseStack.mulPose(Axis.YP.rotation(-side * fold));
                poseStack.scale(side * scale, -scale, scale);
                BodyEffects.draw(sheet, poseStack, red, green, blue);
                for (int k = 0; trail && k < 2; k++) {
                    int p = random.nextInt(sheet.count());
                    if (!BodyEffects.toWorld(poseStack, sheet.x(p), sheet.y(p), 0, WORLD)) break;
                    MoteParticles.spawn(WORLD.x, WORLD.y, WORLD.z, (random.nextFloat() - 0.5F) * 0.01F, -0.015, (random.nextFloat() - 0.5F) * 0.01F, 0.96F,
                            0.002F, TRAIL_LIFE, PIXEL * 0.8F, MoteParticles.SHRINK, sheet.rgb(p), 0.8F, MoteParticles.FADE, LOOK);
                }
                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }
}
