package com.example;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class KeyBoard {
    // 键盘监听
    public static void AddKey(JComponent jc, String key, String actionName, Runnable action) {
        InputMap inputMap = jc.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = jc.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(key), actionName);
        actionMap.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}