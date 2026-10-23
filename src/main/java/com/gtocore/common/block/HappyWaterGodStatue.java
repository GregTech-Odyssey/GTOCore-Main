package com.gtocore.common.block;

import com.gtocore.client.renderer.HappyWaterGodStatueRenderer;
import com.gtocore.common.data.GTOBlockEntities;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.integration.Mods;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.Dimension;
import com.gtolib.quest.QuestLedger;
import com.gtolib.quest.QuestMilestones;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.registrate.util.entry.BlockEntry;
import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/** 毕业雕像：上下两格，下格是底座和方块实体，上格只负责碰撞与点击；右键点亮后发光并三十秒内越来越快地喷出监工可乐，随后变成迪斯科球放曲子；潜行右键直接进入迪斯科。 */
@DataGeneratorScanned
public final class HappyWaterGodStatue extends HorizontalDirectionalBlock implements EntityBlock {

    @RegisterLanguage(cn = "无限快乐水之神", en = "God of Infinite Happy Water")
    public static final String NAMEPLATE = "gtocore.happy_water_god_statue.nameplate";
    @RegisterLanguage(cn = "我才不会右键她~", en = "I would never right-click her~")
    public static final String LORE = "gtocore.happy_water_god_statue.lore";
    @RegisterLanguage(cn = "%s，感谢你游玩GTO", en = "%s, thanks for playing GTO")
    public static final String BUBBLE = "gtocore.happy_water_god_statue.bubble";
    @RegisterLanguage(cn = "格雷科技：新征程整合包完全免费，很开心你能看到此信息，我们很开心有实力的富哥支持一手 %s\n%s",
                      en = "GregTech Odyssey is completely free. We're glad you've seen this message, and we'd love some support from anyone who can spare it: %s\n%s")
    public static final String SPONSOR = "gtocore.happy_water_god_statue.sponsor";
    @RegisterLanguage(cn = "点击打开赞助页面", en = "Click to open the sponsor page")
    public static final String SPONSOR_HOVER = "gtocore.happy_water_god_statue.sponsor_hover";
    private static final String SPONSOR_URL = "https://afdian.com/a/suried";
    private static final String PATREON_URL = "https://www.patreon.com/c/Suried";
    @RegisterLanguage(cn = "光敏性癫痫警告", en = "Photosensitive Seizure Warning")
    public static final String WARNING = "gtocore.happy_water_god_statue.warning";
    @RegisterLanguage(cn = "即将出现强烈闪光与快速变换的光效", en = "Intense flashing and rapidly changing lights ahead")
    public static final String WARNING_DETAIL = "gtocore.happy_water_god_statue.warning_detail";
    @RegisterLanguage(cn = "若感受到不适，请用手遮挡眼睛", en = "If you feel unwell, cover your eyes with your hand")
    public static final String WARNING_HINT = "gtocore.happy_water_god_statue.warning_hint";
    @RegisterLanguage(cn = "请在空旷区域放置奖杯", en = "Please place the trophy in an open area")
    public static final String CROWDED = "gtocore.happy_water_god_statue.crowded";
    @RegisterLanguage(cn = "迈入 %s 时代", en = "Entered the %s Age")
    public static final String RECAP_ERA = "gtocore.happy_water_god_statue.recap.era";
    @RegisterLanguage(cn = "登陆 %s", en = "Landed on %s")
    public static final String RECAP_LAND = "gtocore.happy_water_god_statue.recap.land";
    @RegisterLanguage(cn = "抵达 %s", en = "Arrived at %s")
    public static final String RECAP_ARRIVE = "gtocore.happy_water_god_statue.recap.arrive";
    @RegisterLanguage(cn = "砍下第一棵树", en = "Chopped the first tree")
    public static final String RECAP_TREE = "gtocore.happy_water_god_statue.recap.tree";
    @RegisterLanguage(cn = "挖到第一块矿石", en = "Mined the first ore")
    public static final String RECAP_ORE = "gtocore.happy_water_god_statue.recap.ore";
    @RegisterLanguage(cn = "第 %s 天 · %s 小时 %s 分 %s 秒", en = "Day %s · %sh %sm %ss")
    public static final String RECAP_TIME = "gtocore.happy_water_god_statue.recap.time";
    @RegisterLanguage(cn = "由 %s 达成", en = "Achieved by %s")
    public static final String RECAP_BY = "gtocore.happy_water_god_statue.recap.by";
    @RegisterLanguage(cn = "AE 网络检测", en = "Detected by ME network")
    public static final String RECAP_AE = "gtocore.happy_water_god_statue.recap.ae";
    @RegisterLanguage(cn = "前置完成后自动完成", en = "Completed on unlock")
    public static final String RECAP_UNLOCK = "gtocore.happy_water_god_statue.recap.unlock";
    @RegisterLanguage(cn = "队伍同步", en = "Team sync")
    public static final String RECAP_TEAM = "gtocore.happy_water_god_statue.recap.team";
    @RegisterLanguage(cn = "OP 指令", en = "Operator command")
    public static final String RECAP_OP = "gtocore.happy_water_god_statue.recap.op";
    @RegisterLanguage(cn = "附近 48 格内没有雕像", en = "No statue within 48 blocks")
    public static final String RECAP_NO_STATUE = "gtocore.happy_water_god_statue.recap.no_statue";
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty DISCO = BooleanProperty.create("disco");
    private static final int SPRAY_TICKS = 460;
    private static final int DISCO_TICKS = 460;
    public static final int COUNTDOWN_TICKS = 100;
    public static final long IDLE = Long.MIN_VALUE;
    public static final int WARNING_EVERY = 40;
    public static final String LEAD = "suriedxinxin";
    public static final String[] DEVS = { "POWSDSSAG", "hophenby" };
    public static final String[] HELPERS = { "Alcox233", "maple197", "bnw_neurs", "qwerty54188", "Armadillo", "ratonhagaton" };
    public static final String OFFLINE_HELPER = "Armadillo";
    @RegisterLanguage(cn = "犰狳", en = "Armadillo")
    public static final String OFFLINE_HELPER_NAME = "gtocore.happy_water_god_statue.armadillo";
    private static final int MAX_CREW = 12;
    public static final Set<Entity> CLIENT_SHOWS = Collections.newSetFromMap(new WeakHashMap<>());
    public static final int SONG_LEAD = 60;
    public static final int SONG_FADE = 120;
    public static final int SONG_BAR = 32;
    public static final int SONG_BEAT = 8;
    public static final byte PART_GROOVE = 0, PART_WAVE = 1, PART_PUMP = 2, PART_CHORUS = 3, PART_POINT = 4, PART_SWAY = 5, PART_SPIN = 6, PART_FAREWELL = 7;
    private static final double SOUND_FULL = 32;
    private static final double SOUND_HALF = 48;
    private static final double SOUND_FADE = 96;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape LOWER_SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 8, 16), Block.box(4, 8, 4, 12, 16, 12));
    private static final VoxelShape UPPER_SHAPE = Block.box(3, 0, 3, 13, 15, 13);

    public HappyWaterGodStatue(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false).setValue(DISCO, false).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public static int light(BlockState state) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, DISCO, HALF);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            return neighbor.is(this) && neighbor.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
                if (!player.isCreative()) dropResources(lower, level, below, level.getBlockEntity(below), player, player.getMainHandItem());
                level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, below, Block.getId(lower));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? Collections.emptyList() : super.getDrops(state, params);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            BlockState lower = level.getBlockState(lowerPos);
            if (!lower.is(this)) return InteractionResult.PASS;
            if (!(level.getBlockEntity(lowerPos) instanceof Entity entity) || !(level instanceof ServerLevel server)) return InteractionResult.PASS;
            if (entity.start != IDLE) {
                entity.stop(server);
                sound(level, lowerPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
            } else if (!QuestMilestones.finaleUnlocked(player)) {
                if (player.isShiftKeyDown()) return InteractionResult.PASS;
                entity.begin(server, player, Collections.emptyList(), true);
            } else {
                List<String> crew = gatherCrew(server, player);
                BlockPos blocked = blocked(server, lowerPos, lower.getValue(FACING), 3 + HELPERS.length + crew.size());
                if (blocked != null) {
                    player.sendSystemMessage(Component.translatable(CROWDED).withStyle(ChatFormatting.RED));
                } else {
                    entity.begin(server, player, crew, false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new Entity(GTOBlockEntities.HAPPY_WATER_GOD_STATUE.get(), pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != GTOBlockEntities.HAPPY_WATER_GOD_STATUE.get()) return null;
        return (l, p, s, be) -> ((Entity) be).serverTick((ServerLevel) l, p);
    }

    private static final class Song {

        static final int STEP = 2;
        static final int BAR = 16 * STEP;
        static final int HARP = 0, BASS = 1, BIT = 2, PLING = 3, BELL = 4, FLUTE = 5, KICK = 6, SNARE = 7, HAT = 8;
        static final SoundEvent[] SOUNDS = { SoundEvents.NOTE_BLOCK_HARP.value(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundEvents.NOTE_BLOCK_BELL.value(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundEvents.NOTE_BLOCK_SNARE.value(), SoundEvents.NOTE_BLOCK_HAT.value() };
        static final int[] BASE = { 66, 42, 66, 66, 90, 78, 0, 0, 0 };
        static final String[] MELODY_A = { "E5 . G4 C5 E5 . D5 C5", "A4 . C5 E5 D5 . C5 A4", "F4 A4 C5 . F5 E5 D5 C5", "D5 . B4 G4 B4 D5 . .",
                "E5 . E5 F5 E5 D5 C5 .", "C5 . A4 C5 E5 . D5 C5", "F5 . E5 D5 C5 . A4 C5", "D5 . G4 B4 D5 . C5 ." };
        static final String[] MELODY_B = { "A4 . A4 C5 . A4 C5 F5", "G4 . G4 B4 . G4 B4 D5", "E5 . D5 B4 G4 . B4 D5", "C5 . B4 A4 E4 . A4 C5",
                "F5 . E5 F5 . E5 C5 A4", "D5 . C5 D5 . B4 G4 B4", "D5 F5 D5 A4 F4 A4 D5 F5", "G4 B4 D5 G4 B4 D5 F5 ." };
        static final String[] MELODY_C = { "C5 . C5 D5 E5 . G4 .", "D5 . D5 E5 D5 . B4 .", "E5 . E5 F5 E5 D5 C5 .", "A4 . C5 . F5 . E5 D5",
                "C5 . C5 D5 E5 . G4 .", "D5 . D5 E5 D5 . B4 .", "A4 C5 F5 . E5 D5 C5 D5", "C5 . G4 . C5 . . ." };
        static final String[] CHORDS_A = { "C", "Am", "F", "G", "C", "Am", "F", "G" };
        static final String[] CHORDS_B = { "F", "G", "Em", "Am", "F", "G", "Dm", "G" };
        static final String[] CHORDS_C = { "C", "G", "Am", "F", "C", "G", "F", "C" };
        static final String[] CHORDS_I = { "Am", "F", "C", "G", "Am", "F", "G", "G" };
        static final int LENGTH;
        static final byte[] PARTS;
        static final int[][] AT;
        static final float[][] PITCH;
        static final float[][] VOLUME;
        private static final java.util.List<int[]> RAW = new java.util.ArrayList<>();
        private static final java.util.List<float[]> RAW_F = new java.util.ArrayList<>();

        static {
            byte[] parts = new byte[256];
            int bar = 0;
            bar = mark(parts, bar, intro(bar), PART_GROOVE);
            bar = mark(parts, bar, section(bar, CHORDS_A, MELODY_A, HARP, -1, false, false), PART_WAVE);
            bar = mark(parts, bar, section(bar, CHORDS_A, MELODY_A, PLING, -1, false, false), PART_WAVE);
            bar = mark(parts, bar, section(bar, CHORDS_B, MELODY_B, HARP, -1, false, false), PART_PUMP);
            bar = mark(parts, bar, section(bar, CHORDS_C, MELODY_C, HARP, FLUTE, true, false), PART_CHORUS);
            bar = mark(parts, bar, interlude(bar), PART_POINT);
            bar = mark(parts, bar, section(bar, CHORDS_A, MELODY_A, PLING, -1, false, false), PART_WAVE);
            bar = mark(parts, bar, section(bar, CHORDS_B, MELODY_B, HARP, -1, false, false), PART_PUMP);
            bar = mark(parts, bar, section(bar, CHORDS_C, MELODY_C, HARP, FLUTE, true, false), PART_CHORUS);
            bar = mark(parts, bar, breakdown(bar), PART_SWAY);
            bar = mark(parts, bar, section(bar, CHORDS_C, MELODY_C, HARP, BELL, true, true), PART_SPIN);
            bar = mark(parts, bar, section(bar, CHORDS_C, MELODY_C, HARP, BELL, true, true), PART_SPIN);
            bar = mark(parts, bar, finale(bar), PART_SPIN);
            bar = mark(parts, bar, farewell(bar), PART_FAREWELL);
            LENGTH = bar * BAR;
            PARTS = java.util.Arrays.copyOf(parts, bar);
            int[] counts = new int[LENGTH];
            for (int[] n : RAW) counts[n[0]]++;
            AT = new int[LENGTH][];
            PITCH = new float[LENGTH][];
            VOLUME = new float[LENGTH][];
            for (int t = 0; t < LENGTH; t++) {
                AT[t] = new int[counts[t]];
                PITCH[t] = new float[counts[t]];
                VOLUME[t] = new float[counts[t]];
                counts[t] = 0;
            }
            for (int i = 0; i < RAW.size(); i++) {
                int[] n = RAW.get(i);
                int k = counts[n[0]]++;
                AT[n[0]][k] = n[1];
                PITCH[n[0]][k] = RAW_F.get(i)[0];
                VOLUME[n[0]][k] = RAW_F.get(i)[1];
            }
            RAW.clear();
            RAW_F.clear();
        }

        private static int mark(byte[] parts, int from, int to, byte part) {
            java.util.Arrays.fill(parts, from, to, part);
            return to;
        }

        private static void note(int bar, int step, int instrument, int midi, float volume) {
            float pitch = instrument >= KICK ? (instrument == HAT ? 1.3F : 1.0F) : (float) Math.pow(2, (midi - BASE[instrument]) / 12.0);
            RAW.add(new int[] { bar * BAR + step * STEP, instrument });
            RAW_F.add(new float[] { Mth.clamp(pitch, 0.5F, 2.0F), volume * 0.5F });
        }

        private static int fit(int midi, int low, int high) {
            while (midi > high) midi -= 12;
            while (midi < low) midi += 12;
            return midi;
        }

        private static int midi(String token) {
            int n = switch (token.charAt(0)) {
                case 'C' -> 0;
                case 'D' -> 2;
                case 'E' -> 4;
                case 'F' -> 5;
                case 'G' -> 7;
                case 'A' -> 9;
                default -> 11;
            };
            if (token.length() == 3) n += token.charAt(1) == 'b' ? -1 : 1;
            return 12 * (token.charAt(token.length() - 1) - '0' + 1) + n;
        }

        private static int[] triad(String chord) {
            return switch (chord) {
                case "C" -> new int[] { 60, 64, 67 };
                case "Dm" -> new int[] { 62, 65, 69 };
                case "Em" -> new int[] { 64, 67, 71 };
                case "F" -> new int[] { 65, 69, 72 };
                case "G" -> new int[] { 67, 71, 74 };
                case "Bb" -> new int[] { 58, 62, 65 };
                case "Gm" -> new int[] { 55, 58, 62 };
                default -> new int[] { 69, 72, 76 };
            };
        }

        private static int[] bassNotes(String chord) {
            return switch (chord) {
                case "C" -> new int[] { 48, 43 };
                case "Dm" -> new int[] { 50, 45 };
                case "Em" -> new int[] { 40, 47 };
                case "F" -> new int[] { 41, 48 };
                case "G" -> new int[] { 43, 50 };
                case "Bb" -> new int[] { 46, 41 };
                case "Gm" -> new int[] { 43, 50 };
                default -> new int[] { 45, 52 };
            };
        }

        private static void drums(int bar, boolean busy, boolean fill) {
            note(bar, 0, KICK, 0, 0.9F);
            note(bar, 8, KICK, 0, 0.9F);
            if (busy) note(bar, 10, KICK, 0, 0.6F);
            note(bar, 4, SNARE, 0, 0.7F);
            note(bar, 12, SNARE, 0, 0.7F);
            for (int st = 2; st < 16; st += busy ? 2 : 4) if (st % 4 != 0 || busy) note(bar, st, HAT, 0, 0.35F);
            if (fill) for (int st = 13; st < 16; st++) note(bar, st, SNARE, 0, 0.55F);
        }

        private static void bass(int bar, String chord) {
            int[] b = bassNotes(chord);
            note(bar, 0, BASS, b[0], 0.9F);
            note(bar, 4, BASS, b[1], 0.7F);
            note(bar, 6, BASS, b[0], 0.6F);
            note(bar, 8, BASS, b[0], 0.9F);
            note(bar, 12, BASS, b[1], 0.7F);
            note(bar, 14, BASS, b[0], 0.6F);
        }

        private static void melody(int bar, String line, int instrument, float volume, int octave) {
            String[] tokens = line.split(" ");
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].equals(".")) continue;
                note(bar, i * 2, instrument, midi(tokens[i]) + octave, volume);
            }
        }

        private static void arp(int bar, String chord, int instrument, float volume) {
            int[] t = triad(chord);
            int[] order = { 0, 1, 2, 1 };
            for (int st = 0; st < 16; st++) note(bar, st, instrument, fit(t[order[st % 4]] + (st >= 8 ? 12 : 0), 54, 78), volume);
        }

        private static int section(int bar, String[] chords, String[] lines, int lead, int doubled, boolean busy, boolean sparkle) {
            for (int i = 0; i < 8; i++) {
                drums(bar + i, busy, i == 7);
                bass(bar + i, chords[i]);
                int[] t = triad(chords[i]);
                for (int note : t) {
                    note(bar + i, 4, PLING, note, 0.22F);
                    note(bar + i, 12, PLING, note, 0.22F);
                }
                melody(bar + i, lines[i], lead, 1.0F, 0);
                if (doubled == FLUTE) melody(bar + i, lines[i], FLUTE, 0.45F, 12);
                if (doubled == BELL) melody(bar + i, lines[i], BELL, 0.55F, 12);
                if (sparkle) arp(bar + i, chords[i], BIT, 0.25F);
            }
            return bar + 8;
        }

        private static int finale(int bar) {
            note(bar, 0, KICK, 0, 1.0F);
            for (int n : triad(CHORDS_C[0])) {
                note(bar, 0, BELL, n + 12, 0.7F);
                note(bar, 0, PLING, n, 0.6F);
            }
            section(bar, CHORDS_C, MELODY_C, HARP, BELL, true, true);
            for (int i = 0; i < 8; i++) {
                melody(bar + i, MELODY_C[i], FLUTE, 0.5F, 12);
                note(bar + i, 4, KICK, 0, 0.75F);
                note(bar + i, 12, KICK, 0, 0.75F);
            }
            for (int n : triad(CHORDS_C[7])) note(bar + 7, 8, BELL, n + 12, 0.6F);
            return bar + 8;
        }

        private static int intro(int bar) {
            String[] chords = { "C", "Am", "F", "G" };
            for (int i = 0; i < 4; i++) {
                drums(bar + i, false, i == 3);
                bass(bar + i, chords[i]);
                for (int note : triad(chords[i])) note(bar + i, 0, PLING, note, 0.4F);
            }
            return bar + 4;
        }

        private static int interlude(int bar) {
            for (int i = 0; i < 8; i++) {
                drums(bar + i, true, i == 7);
                bass(bar + i, CHORDS_I[i]);
                arp(bar + i, CHORDS_I[i], BIT, 0.6F);
                int[] t = triad(CHORDS_I[i]);
                note(bar + i, 0, BELL, t[0] + 24, 0.6F);
                note(bar + i, 8, BELL, t[2] + 12, 0.5F);
            }
            return bar + 8;
        }

        private static int breakdown(int bar) {
            String[] chords = { "F", "G", "Am", "Am", "F", "G", "G", "G" };
            for (int i = 0; i < 8; i++) {
                bass(bar + i, chords[i]);
                for (int st = 2; st < 16; st += 4) note(bar + i, st, HAT, 0, 0.3F);
                if (i >= 4) {
                    int every = i < 6 ? 4 : i == 6 ? 2 : 1;
                    for (int st = 0; st < 16; st += every) note(bar + i, st, SNARE, 0, 0.35F + 0.08F * (i - 4) + st * 0.01F);
                    int[] t = triad(chords[i]);
                    for (int st = 0; st < 16; st += 2) note(bar + i, st, HARP, fit(t[(st / 2) % 3] + (st >= 8 ? 12 : 0), 54, 78), 0.5F);
                }
            }
            return bar + 8;
        }

        private static int farewell(int bar) {
            int[] rise = { 55, 58, 60, 64, 67, 70, 72, 76 };
            note(bar, 0, BASS, 48, 0.6F);
            note(bar, 8, BASS, 46, 0.5F);
            note(bar, 0, BELL, 82, 0.3F);
            for (int i = 0; i < 8; i++) note(bar, i * 2, HARP, rise[i], 0.3F + 0.03F * i);
            String[] chords = { "F", "Dm", "Bb", "C", "F", "Am", "Bb", "C", "Dm", "Bb", "Gm", "C", "Bb", "F", "F" };
            String[] lines = { "A5 . . . G5 . F5 .", "A5 . . . D5 . . .", "D6 . . . C6 . Bb5 .", "A5 . . . G5 . . .", "A5 . C6 . A5 . G5 F5",
                    "E5 . . . A5 . . .", "Bb5 . A5 . G5 . F5 .", "G5 . . . . . . .", "F5 . A5 . D6 . . .", "D6 . C6 . Bb5 . A5 .", "Bb5 . . . A5 . G5 .",
                    "A5 . . . G5 . . .", "F5 . G5 . A5 . . .", "F5 . . . . . . .", ". . . . . . . ." };
            int[] order = { 0, 1, 2, 1 };
            for (int i = 0; i < chords.length; i++) {
                int b = bar + 1 + i;
                float dyn = 1.0F - 0.55F * i / (chords.length - 1);
                int[] t = triad(chords[i]);
                int[] low = bassNotes(chords[i]);
                note(b, 0, BASS, low[0], 0.55F * dyn);
                note(b, 8, BASS, low[1], 0.4F * dyn);
                if (i < chords.length - 1) {
                    for (int st = 0; st < 16; st += 2) note(b, st, HARP, fit(t[order[(st / 2) % 4]] + (st >= 8 ? 12 : 0), 54, 78), 0.3F * dyn);
                    melody(b, lines[i], FLUTE, 0.85F * dyn, 0);
                    melody(b, lines[i], BELL, 0.2F * dyn, 0);
                } else {
                    for (int st = 0; st < 16; st += 4) {
                        for (int n : t) note(b, st, PLING, n, 0.3F * dyn * (1 - st / 20.0F));
                    }
                    note(b, 0, BELL, 89, 0.3F * dyn);
                    note(b, 0, FLUTE, 77, 0.5F * dyn);
                }
            }
            return bar + 1 + chords.length;
        }
    }

    public static float songTime(float discoTicks) {
        return discoTicks - SONG_LEAD;
    }

    public static int songLength() {
        return Song.LENGTH;
    }

    public static int showLength(boolean shortcut, boolean prelude) {
        if (prelude) return DISCO_TICKS;
        return (shortcut ? 0 : DISCO_TICKS) + SONG_LEAD + Song.LENGTH + SONG_FADE;
    }

    public static int musicTick(int elapsed, boolean shortcut) {
        return elapsed - (shortcut ? 0 : DISCO_TICKS) - SONG_LEAD;
    }

    public static int notes(int tick) {
        return tick < 0 || tick >= Song.LENGTH ? 0 : Song.AT[tick].length;
    }

    public static SoundEvent noteSound(int tick, int i) {
        return Song.SOUNDS[Song.AT[tick][i]];
    }

    public static float notePitch(int tick, int i) {
        return Song.PITCH[tick][i];
    }

    public static float noteVolume(int tick, int i) {
        return Math.min(1.0F, Song.VOLUME[tick][i] * 2.0F);
    }

    public static float soundGain(double d) {
        if (d >= SOUND_FADE) return 0;
        return (float) (d <= SOUND_FULL ? 1 : d <= SOUND_HALF ? 1 - 0.5 * (d - SOUND_FULL) / (SOUND_HALF - SOUND_FULL) : 0.5 * (SOUND_FADE - d) / (SOUND_FADE - SOUND_HALF));
    }

    public static byte songPart(float songTime) {
        if (songTime < 0) return PART_GROOVE;
        if (songTime >= Song.LENGTH) return PART_FAREWELL;
        return Song.PARTS[(int) (songTime / Song.BAR)];
    }

    static void sound(Level level, BlockPos pos, SoundEvent event, SoundSource source, float volume, float pitch) {
        if (!(level instanceof ServerLevel server)) return;
        double sx = pos.getX() + 0.5, sy = pos.getY() + 1.0, sz = pos.getZ() + 0.5;
        Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event);
        long seed = server.random.nextLong();
        for (ServerPlayer player : server.players()) {
            double dx = sx - player.getX(), dy = sy - player.getEyeY(), dz = sz - player.getZ();
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d >= SOUND_FADE) continue;
            double gain = soundGain(d);
            double near = d < 1.0E-3 ? 0 : Math.min(d, 1.0) / d;
            float v = (float) Math.min(1.0, volume * gain / (1 - near * d / 16));
            player.connection.send(new ClientboundSoundPacket(holder, source, player.getX() + dx * near, player.getEyeY() + dy * near, player.getZ() + dz * near, v, pitch, seed));
        }
    }

    public static final class StatueItem extends BlockItem {

        public StatueItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable(LORE).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(new IClientItemExtensions() {

                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    return HappyWaterGodStatueRenderer.ItemRenderer.get();
                }
            });
        }
    }

    public static final class Entity extends BlockEntity {

        private static final String SHOW_KEY = "show";
        private static final StreamCodec<FriendlyByteBuf, Show> SHOW = StreamCodec.composite(
                ByteBufCodecs.LONG, Show::start,
                ByteBufCodecs.INT, Show::shortcut,
                ByteBufCodecs.collection(ObjectArrayList::new, ByteBufCodecs.STRING_UTF8), Show::crew,
                QuestMilestones.SHOWN_LIST, Show::recap,
                Show::new);

        private record Show(long start, int shortcut, List<String> crew, List<QuestMilestones.Shown> recap) {}

        public Object dancer;
        public int heard = Integer.MIN_VALUE;
        private long start = IDLE;
        private boolean shortcut, prelude;
        private List<String> crew = Collections.emptyList();
        private List<QuestMilestones.Shown> recap = Collections.emptyList();
        private int spray;
        private float pending;

        public Entity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        public long start() {
            return start;
        }

        public boolean shortcut() {
            return shortcut;
        }

        public boolean prelude() {
            return prelude;
        }

        public List<String> crew() {
            return crew;
        }

        public List<QuestMilestones.Shown> recap() {
            return recap;
        }

        public void preview(long start, List<QuestMilestones.Shown> recap) {
            this.start = start;
            this.shortcut = true;
            this.prelude = false;
            this.crew = Collections.emptyList();
            this.recap = recap;
            heard = Integer.MIN_VALUE;
            dancer = null;
        }

        private void begin(ServerLevel level, Player player, List<String> crew, boolean prelude) {
            start = level.getGameTime() + COUNTDOWN_TICKS;
            this.prelude = prelude;
            shortcut = !prelude && player.isShiftKeyDown();
            this.crew = crew;
            recap = prelude ? Collections.emptyList() : QuestMilestones.select(level.getServer(), player);
            midnight(level);
            sync(level);
        }

        private static Component sponsorMessage() {
            return Component.translatable(SPONSOR, sponsorLink(SPONSOR_URL), sponsorLink(PATREON_URL)).withStyle(ChatFormatting.GOLD);
        }

        private static Component sponsorLink(String url) {
            return Component.literal(url).withStyle(style -> style.withColor(ChatFormatting.AQUA).withUnderlined(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(SPONSOR_HOVER))));
        }

        private void stop(ServerLevel level) {
            start = IDLE;
            spray = 0;
            recap = Collections.emptyList();
            setLit(level, false);
            sync(level);
        }

        private void sync(ServerLevel level) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }

        private void setLit(ServerLevel level, boolean on) {
            BlockState state = getBlockState();
            if (state.getValue(LIT) == on) return;
            level.setBlock(worldPosition, state.setValue(LIT, on).setValue(DISCO, on && shortcut), Block.UPDATE_ALL);
            BlockState upper = level.getBlockState(worldPosition.above());
            if (upper.is(state.getBlock())) level.setBlock(worldPosition.above(), upper.setValue(LIT, on).setValue(DISCO, on && shortcut), Block.UPDATE_ALL);
        }

        private void serverTick(ServerLevel level, BlockPos pos) {
            if (start == IDLE) {
                if (getBlockState().getValue(LIT)) setLit(level, false);
                return;
            }
            long t = level.getGameTime() - start;
            if (t < 0) return;
            if (t >= showLength(shortcut, prelude)) {
                stop(level);
                level.getServer().getPlayerList().broadcastSystemMessage(sponsorMessage(), false);
                return;
            }
            if (!getBlockState().getValue(LIT)) {
                setLit(level, true);
                spray = shortcut || t > 0 ? 0 : SPRAY_TICKS;
                pending = 0;
                sound(level, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.2F);
            }
            if (spray <= 0) return;
            spray--;
            float s = (SPRAY_TICKS - spray) / 20.0F;
            float ramp = s < 8 ? 0 : Math.min(1, (s - 8) / 12.0F);
            pending += 1.0F / 3.0F + (6.0F - 1.0F / 3.0F) * ramp;
            var random = level.random;
            var drinks = GTOItems.MYSTERIOUS_BOOST_DRINK;
            boolean fired = false;
            while (pending >= 1) {
                pending -= 1;
                var entry = drinks[random.nextInt(drinks.length)];
                if (entry == null) continue;
                ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, new ItemStack(entry.get()));
                float yaw = random.nextFloat() * Mth.TWO_PI;
                float speed = (0.35F + random.nextFloat() * 0.55F) * (1 + 1.2F * ramp);
                item.setDeltaMovement(Mth.cos(yaw) * speed, (0.35F + random.nextFloat() * 0.55F) * (1 + 0.6F * ramp), Mth.sin(yaw) * speed);
                item.setPickUpDelay(15);
                item.lifespan = 160;
                level.addFreshEntity(item);
                fired = true;
            }
            if (fired) sound(level, pos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 0.8F, 1.2F + 0.8F * ramp + random.nextFloat() * 0.2F);
            if (spray % 10 == 0 && ramp > 0) sound(level, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 0.7F, 1.4F + 0.6F * ramp);
        }

        @Override
        public CompoundTag getUpdateTag() {
            CompoundTag tag = super.getUpdateTag();
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            try {
                SHOW.encode(buf, new Show(start, prelude ? 2 : shortcut ? 1 : 0, crew, recap));
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                tag.putByteArray(SHOW_KEY, bytes);
            } finally {
                buf.release();
            }
            return tag;
        }

        @Override
        public Packet<ClientGamePacketListener> getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }

        @Override
        public void load(CompoundTag tag) {
            super.load(tag);
            if (!tag.contains(SHOW_KEY)) return;
            Show show = SHOW.decode(new FriendlyByteBuf(Unpooled.wrappedBuffer(tag.getByteArray(SHOW_KEY))));
            if (show.start() != start) {
                heard = Integer.MIN_VALUE;
                dancer = null;
            }
            start = show.start();
            shortcut = show.shortcut() == 1;
            prelude = show.shortcut() == 2;
            crew = show.crew();
            recap = show.recap();
        }

        @Override
        public void onLoad() {
            super.onLoad();
            if (level != null && level.isClientSide) CLIENT_SHOWS.add(this);
        }

        @Override
        public void setRemoved() {
            super.setRemoved();
            CLIENT_SHOWS.remove(this);
        }

        @Override
        public AABB getRenderBoundingBox() {
            return new AABB(worldPosition).inflate(96);
        }
    }

    private static List<String> gatherCrew(ServerLevel level, Player player) {
        UUID self = player.getUUID();
        List<UUID> ids = new ObjectArrayList<>();
        ids.add(self);
        if (Mods.FTBTEAMS.isLoaded()) {
            for (UUID id : Ftb.members(self)) if (!id.equals(self)) ids.add(id);
        }
        var server = level.getServer();
        var cache = server.getProfileCache();
        List<String> names = new ObjectArrayList<>();
        for (UUID id : ids) {
            ServerPlayer online = server.getPlayerList().getPlayer(id);
            String name = online != null ? online.getGameProfile().getName() : cache == null ? null : cache.get(id).map(GameProfile::getName).orElse(null);
            if (name == null || isDev(name)) continue;
            names.add(name);
            if (names.size() >= MAX_CREW) break;
        }
        return names;
    }

    public static List<QuestMilestones.Shown> fakeRecap(String player) {
        var list = new ObjectArrayList<QuestMilestones.Shown>(40);
        long[] clock = { 1, 0 };
        String[] sources = { player, player, "", player, player, "" };
        byte[] silent = { (byte) QuestLedger.Source.AE_DETECTOR.ordinal(), (byte) QuestLedger.Source.UNLOCK.ordinal(), (byte) QuestLedger.Source.TEAM_SYNC.ordinal() };
        int[] turn = { 0 };
        java.util.function.BiConsumer<Byte, String[]> add = (type, data) -> {
            if (list.size() >= QuestMilestones.MAX_SHOWN) return;
            clock[0] += 2 + clock[0] / 6;
            clock[1] += 20L * (900 + 1800L * list.size() + 37L * list.size() * list.size());
            String who = sources[turn[0] % sources.length];
            byte source = who.isEmpty() ? silent[turn[0] / sources.length % silent.length] : (byte) QuestLedger.Source.PLAYER_ACTION.ordinal();
            turn[0]++;
            list.add(new QuestMilestones.Shown(type, data[0], data[1], clock[0], clock[1], who, source));
        };
        add.accept(QuestMilestones.TREE, new String[] { "minecraft:oak_log", "" });
        add.accept(QuestMilestones.ORE, new String[] { "minecraft:iron_ore", "" });
        BlockEntry<?>[] casings = { GTBlocks.MACHINE_CASING_ULV, GTBlocks.MACHINE_CASING_LV, GTBlocks.MACHINE_CASING_MV, GTBlocks.MACHINE_CASING_HV, GTBlocks.MACHINE_CASING_EV,
                GTBlocks.MACHINE_CASING_IV, GTBlocks.MACHINE_CASING_LuV, GTBlocks.MACHINE_CASING_ZPM, GTBlocks.MACHINE_CASING_UV, GTBlocks.MACHINE_CASING_UHV };
        MachineDefinition[][] machines = { GTMachines.ELECTRIC_FURNACE, GTMachines.MACERATOR, GTMachines.CHEMICAL_REACTOR, GTMachines.ASSEMBLER, GTMachines.CENTRIFUGE,
                GTMachines.ELECTROLYZER, GTMachines.LASER_ENGRAVER, GTMachines.MIXER, GTMachines.WIREMILL };
        Dimension[][] travel = { {}, { Dimension.THE_NETHER }, {}, { Dimension.MOON }, { Dimension.MARS, Dimension.THE_END }, { Dimension.VENUS, Dimension.MERCURY },
                { Dimension.CERES, Dimension.ANCIENT_WORLD }, { Dimension.IO, Dimension.GANYMEDE }, { Dimension.TITAN }, { Dimension.PLUTO } };
        for (int tier = 0; tier < casings.length; tier++) {
            add.accept(QuestMilestones.ERA, new String[] { id(casings[tier].asItem()), String.valueOf(tier) });
            if (tier >= GTValues.LV) {
                var machine = machines[(tier - 1) % machines.length][tier];
                if (machine != null) add.accept(QuestMilestones.MACHINE, new String[] { id(machine.asItem()), "" });
                var hatch = GTMachines.ENERGY_INPUT_HATCH[tier];
                if (hatch != null) add.accept(QuestMilestones.HATCH, new String[] { id(hatch.asItem()), "" });
            }
            for (var dim : travel[tier]) add.accept(dim.isWithinGalaxy() ? QuestMilestones.LAND : QuestMilestones.ARRIVE, new String[] { "", dim.getLocation().toString() });
            if (tier == GTValues.IV) {
                var giants = QuestMilestones.giants();
                if (!giants.isEmpty()) add.accept(QuestMilestones.GIANT, new String[] { giants.get(0), "" });
            }
            if (tier == GTValues.UV) add.accept(QuestMilestones.CREATIVE, new String[] { id(GTMachines.CREATIVE_ENERGY.asItem()), "" });
        }
        while (list.size() >= QuestMilestones.MAX_SHOWN) list.remove(list.size() - 1);
        list.add(new QuestMilestones.Shown(QuestMilestones.STATUE, id(GTOBlocks.HAPPY_WATER_GOD_STATUE.asItem()), "", clock[0] + 30, clock[1] + 20L * 7200, player, (byte) QuestLedger.Source.PLAYER_ACTION.ordinal()));
        return list;
    }

    private static String id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static float seatX(int slot) {
        if (slot == 0) return 0;
        if (slot <= 2) return slot == 1 ? 1.8F : -1.8F;
        int seat = slot - 3, pair = seat / 2;
        return ((seat & 1) == 0 ? 1 : -1) * (3.2F + 1.35F * (pair % 3) + 0.65F * (pair / 3));
    }

    public static float seatZ(int slot) {
        if (slot == 0) return 0;
        if (slot <= 2) return -0.7F;
        int pair = (slot - 3) / 2;
        return -0.3F - 0.45F * (pair % 3) - 1.8F * (pair / 3);
    }

    @Nullable
    private static BlockPos blocked(ServerLevel level, BlockPos origin, Direction front, int slots) {
        float fx = front.getStepX(), fz = front.getStepZ(), rx = -fz, rz = fx;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int slot = 0; slot < slots; slot++) {
            float lx = seatX(slot), lz = seatZ(slot);
            int cx = Mth.floor(origin.getX() + 0.5F + rx * lx + fx * lz), cz = Mth.floor(origin.getZ() + 0.5F + rz * lx + fz * lz);
            for (int dy = 1; dy <= 6; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        cursor.set(cx + dx, origin.getY() + dy, cz + dz);
                        if (cursor.getX() == origin.getX() && cursor.getZ() == origin.getZ() && dy == 1) continue;
                        if (!level.getBlockState(cursor).isAir()) return cursor.immutable();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isDev(String name) {
        if (LEAD.equalsIgnoreCase(name)) return true;
        for (String helper : HELPERS) if (helper.equalsIgnoreCase(name)) return true;
        for (String dev : DEVS) if (dev.equalsIgnoreCase(name)) return true;
        return false;
    }

    private static void midnight(ServerLevel level) {
        long now = level.getDayTime();
        long target = now - Math.floorMod(now, 24000L) + 18000L;
        if (target < now) target += 24000L;
        level.setDayTime(target);
    }

    private static final class Ftb {

        static Collection<UUID> members(UUID player) {
            var api = FTBTeamsAPI.api();
            if (!api.isManagerLoaded()) return Collections.singletonList(player);
            return api.getManager().getTeamForPlayerID(player).map(Team::getMembers).map(set -> (Collection<UUID>) set).orElse(Collections.singletonList(player));
        }
    }
}
