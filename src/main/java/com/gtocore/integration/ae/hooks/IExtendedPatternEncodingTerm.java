package com.gtocore.integration.ae.hooks;

import com.gtocore.integration.ae.client.PatternDestinationPanel;

import appeng.client.gui.widgets.ActionButton;

public interface IExtendedPatternEncodingTerm {

    PatternDestinationPanel gto$getPatternDestDisplay();

    IExtendedPatternEncodingTerm.Menu gto$getMenu();

    ActionButton gto$getEncodeButton();

    interface Menu {

        void gtolib$sendPattern(int index);

        void gtolib$sendEncodeRequest();
    }
}
