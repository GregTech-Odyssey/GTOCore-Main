package com.gtocore.integration.ae.hooks;

import com.gtocore.integration.ae.client.PatternDestinationPanel;

import appeng.client.gui.widgets.ActionButton;

public interface IExtendedPatternEncodingTerm {

    PatternDestinationPanel gto$getPatternDestDisplay();

    IExtendedPatternEncodingTerm.Menu gto$getMenu();

    ActionButton gto$getEncodeButton();

    interface Menu {

        /**
         * @param requestId 目的地列表的编号（随列表一起下发），与服务端当前列表不一致时忽略
         * @param index     目的地在列表中的下标
         */
        void gtolib$sendPattern(int requestId, int index);

        /**
         * 请求目的地列表中各目的地已有样板的产物，服务端对同一份列表只响应一次。
         */
        void gtolib$requestPatternOutputs(int requestId);

        void gtolib$sendEncodeRequest();
    }
}
