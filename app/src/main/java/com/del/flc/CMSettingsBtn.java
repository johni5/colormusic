package com.del.flc;

import com.del.flc.utils.StringUtil;

public class CMSettingsBtn extends CMButton {

    public CMSettingsBtn(int cmd) {
        super("Настройка", cmd);
        this.state = -1;
    }

    @Override
    public boolean setValue(String value) {
        Float digit = StringUtil.parseDigit(value);
        return digit != null;
    }
}
