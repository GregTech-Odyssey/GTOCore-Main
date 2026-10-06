package com.gtolib.api.dimension;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 生命周期状态机测试：空闲边界、迟到占用、租约平衡、保存恢复、关闭封禁及重复加载卸载。
 */
class DimensionLifecycleTest {

    @Test
    void idleBoundaryAndLateOccupationAreRechecked() {
        var lifecycle = loaded(0);
        assertFalse(lifecycle.observe(1199, false, 1200));
        assertTrue(lifecycle.observe(1200, false, 1200));
        assertFalse(lifecycle.beginSave(true));
        assertEquals(DimensionLifecycle.State.LOADED, lifecycle.state());
        assertFalse(lifecycle.observe(1200, true, 1200));
        assertFalse(lifecycle.observe(1201, false, 1200));
        assertFalse(lifecycle.observe(2400, false, 1200));
        assertTrue(lifecycle.observe(2401, false, 1200));
    }

    @Test
    void leasesAndOccupationDuringSaveCancelClosing() {
        var lifecycle = loaded(0);
        lifecycle.acquire(DimensionLifecycle.KeepAlive.TRANSFER);
        lifecycle.acquire(DimensionLifecycle.KeepAlive.TRANSFER);
        assertFalse(lifecycle.observe(10000, false, 1200));
        lifecycle.release(DimensionLifecycle.KeepAlive.TRANSFER);
        assertFalse(lifecycle.beginSave(false));
        lifecycle.release(DimensionLifecycle.KeepAlive.TRANSFER);
        assertTrue(lifecycle.beginSave(false));
        lifecycle.acquire(DimensionLifecycle.KeepAlive.BUILD);
        assertFalse(lifecycle.beginClose(false));
        lifecycle.cancelSave(10000);
        lifecycle.release(DimensionLifecycle.KeepAlive.BUILD);
        assertThrows(IllegalStateException.class, () -> lifecycle.release(DimensionLifecycle.KeepAlive.BUILD));
    }

    @Test
    void failedSaveCanRetryButFailedCloseCannotReopen() {
        var lifecycle = loaded(0);
        assertTrue(lifecycle.beginSave(false));
        lifecycle.cancelSave(1200);
        assertFalse(lifecycle.observe(2399, false, 1200));
        assertTrue(lifecycle.observe(2400, false, 1200));
        assertTrue(lifecycle.beginSave(false));
        assertTrue(lifecycle.beginClose(false));
        assertThrows(IllegalStateException.class, () -> lifecycle.acquire(DimensionLifecycle.KeepAlive.TRANSFER));
        lifecycle.poison();
        assertThrows(IllegalStateException.class, lifecycle::beginLoad);
        assertEquals(DimensionLifecycle.State.POISONED, lifecycle.state());
    }

    @Test
    void initializationAndRepeatedCyclesDoNotExposeIncompleteState() {
        var lifecycle = new DimensionLifecycle();
        assertThrows(IllegalStateException.class, () -> lifecycle.loaded(0));
        lifecycle.beginLoad();
        assertThrows(IllegalStateException.class, lifecycle::beginLoad);
        assertFalse(lifecycle.observe(10000, false, 1200));
        lifecycle.loadFailed();
        for (int i = 0; i < 10; i++) {
            lifecycle.beginLoad();
            lifecycle.loaded(i * 2000L);
            assertTrue(lifecycle.beginSave(false));
            assertTrue(lifecycle.beginClose(false));
            lifecycle.closed();
            assertEquals(DimensionLifecycle.State.DORMANT, lifecycle.state());
        }
    }

    private static DimensionLifecycle loaded(long tick) {
        var lifecycle = new DimensionLifecycle();
        lifecycle.beginLoad();
        lifecycle.loaded(tick);
        return lifecycle;
    }
}
