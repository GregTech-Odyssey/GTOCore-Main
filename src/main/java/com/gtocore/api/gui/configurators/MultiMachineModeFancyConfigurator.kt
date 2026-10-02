package com.gtocore.api.gui.configurators

import com.gtocore.common.data.GTORecipeTypes

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.recipe.GTRecipeType

import java.util.Collections
import java.util.function.Consumer

@JvmSuppressWildcards
open class MultiMachineModeFancyConfigurator(recipeTypes: List<GTRecipeType?>, selected: GTRecipeType?, onChange: Consumer<GTRecipeType?>?) : CustomModeFancyConfigurator(calculateModeSize(recipeTypes, selected)) {
    private val recipeTypes = createRecipeTypeList(recipeTypes, selected)
    private val onChange = onChange ?: throw NullPointerException("onChange consumer cannot be null")
    private var currentMode = 0

    init {
        setRecipeType(selected ?: GTORecipeTypes.HATCH_COMBINED)
    }

    private fun getCurrentRecipeType(): GTRecipeType? = recipeTypes[currentMode]

    open fun setRecipeType(recipe: GTRecipeType?) {
        requireNotNull(recipe) { "Recipe type cannot be null" }
        val index = recipeTypes.indexOf(recipe)
        if (index >= 0) setMode(index)
    }

    override fun setMode(index: Int) {
        if (index < 0 || index >= recipeTypes.size) throw IllegalArgumentException("Mode index out of bounds: $index")
        currentMode = index
        onChange.accept(if (getCurrentRecipeType() === GTORecipeTypes.HATCH_COMBINED) null else getCurrentRecipeType())
    }

    override fun getCurrentMode(): Int = currentMode

    override fun getLanguageKey(index: Int): String {
        if (index < 0 || index >= recipeTypes.size) return getLanguageKey(0)
        val recipeType = recipeTypes[index] ?: return getLanguageKey(0)
        return recipeType.registryName.toLanguageKey()
    }

    companion object {
        private val EMPTY_LIST = Collections.singletonList(GTORecipeTypes.HATCH_COMBINED)

        @JvmStatic
        fun extractRecipeTypes(controller: IMultiController?): List<GTRecipeType> = if (controller is IRecipeLogicMachine) controller.getAvailableRecipeTypes().asList() else Collections.emptyList()

        @JvmStatic
        fun verify(recipeTypes: Collection<GTRecipeType?>, currentRecipeType: GTRecipeType?, clearOperation: Runnable) {
            if (currentRecipeType == null || recipeTypes.contains(currentRecipeType)) return
            clearOperation.run()
        }

        private fun calculateModeSize(recipeTypes: List<GTRecipeType?>, selected: GTRecipeType?): Int {
            if (recipeTypes.isEmpty()) return 1
            return recipeTypes.size + if (selected == null || recipeTypes.contains(selected)) 1 else 2
        }

        private fun createRecipeTypeList(original: List<GTRecipeType?>, selected: GTRecipeType?): List<GTRecipeType?> {
            if (original.isEmpty()) return EMPTY_LIST
            return ArrayList(original).apply {
                add(GTORecipeTypes.HATCH_COMBINED)
                if (selected != null && !contains(selected)) add(selected)
            }
        }
    }
}
