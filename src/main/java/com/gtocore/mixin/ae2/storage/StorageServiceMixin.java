package com.gtocore.mixin.ae2.storage;

import com.gtocore.api.ae2.stacks.FuzzyKeyCounter;

import com.gtolib.api.ae2.IExpandedStorageService;

import appeng.api.stacks.KeyCounter;
import appeng.me.service.StorageService;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(StorageService.class)
public abstract class StorageServiceMixin implements IExpandedStorageService {

    @Override
    public FuzzyKeyCounter getFuzzyKeyCounter() {
        return null;
    }

    @Override
    public KeyCounter getLazyKeyCounter() {
        return null;
    }
}
