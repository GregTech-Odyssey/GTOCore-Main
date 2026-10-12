package com.gtocore.common.recipe.condition;

import com.gtocore.api.machine.part.IHeatContainerPart;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.ICoilMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

class HeatConditionTest {

    @Test
    void externalHeatLimitsUseInclusiveMinimumAndExclusiveMaximum() {
        var minimum = new HeatCondition(300);
        var maximum = HeatCondition.maximumTemperature(300);
        for (double temperature : new double[] { 299.5, 300, 300.5 }) {
            var holder = controller(null, heatPart(temperature));
            assertEquals(temperature >= 300, minimum.check(holder, null, null));
            assertEquals(temperature < 300, maximum.check(holder, null, null));
        }
    }

    @Test
    void upperLimitRejectsAnyHotSourceAndRequiresAHeatSource() {
        var maximum = HeatCondition.maximumTemperature(300);
        assertFalse(maximum.check(controller(null), null, null));
        assertFalse(new HeatCondition(300).check(controller(null), null, null));
        assertTrue(maximum.check(controller(299), null, null));
        assertFalse(maximum.check(controller(300), null, null));
        assertTrue(maximum.check(controller(299, heatPart(298)), null, null));
        assertFalse(maximum.check(controller(299, heatPart(300)), null, null));
        assertFalse(maximum.check(controller(null, heatPart(298), heatPart(300)), null, null));
    }

    @Test
    void machineTemperatureLimitUsesItsOwnContainer() {
        var maximum = HeatCondition.maximumMachineTemperature(300);
        for (double temperature : new double[] { 299.5, 300, 300.5 }) {
            var container = heatContainer(temperature);
            var holder = (IRecipeHandlerHolder) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[] { IRecipeHandlerHolder.class, IHeatContainerMachine.class, IMultiController.class },
                    (proxy, method, args) -> {
                        if (method.getName().equals("getHeatContainer")) return container;
                        throw new AssertionError("Unexpected machine method: " + method);
                    });
            assertEquals(temperature < 300, maximum.check(holder, null, null));
        }
    }

    private static IRecipeHandlerHolder controller(Integer coilTemperature, IMultiPart... parts) {
        var types = coilTemperature == null ?
                new Class<?>[] { IRecipeHandlerHolder.class, IMultiController.class } :
                new Class<?>[] { IRecipeHandlerHolder.class, IMultiController.class, ICoilMachine.class };
        return (IRecipeHandlerHolder) Proxy.newProxyInstance(HeatConditionTest.class.getClassLoader(), types,
                (proxy, method, args) -> switch (method.getName()) {
                    case "getParts" -> parts;
                    case "getTemperature" -> coilTemperature;
                    default -> throw new AssertionError("Unexpected controller method: " + method);
                });
    }

    private static IMultiPart heatPart(double temperature) {
        var container = heatContainer(temperature);
        return (IMultiPart) Proxy.newProxyInstance(HeatConditionTest.class.getClassLoader(),
                new Class<?>[] { IMultiPart.class, IHeatContainerPart.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getHeatContainer")) return container;
                    throw new AssertionError("Unexpected heat part method: " + method);
                });
    }

    private static IHeatContainer heatContainer(double temperature) {
        return (IHeatContainer) Proxy.newProxyInstance(HeatConditionTest.class.getClassLoader(), new Class<?>[] { IHeatContainer.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getTemperature")) return temperature;
                    throw new AssertionError("Unexpected heat container method: " + method);
                });
    }
}
