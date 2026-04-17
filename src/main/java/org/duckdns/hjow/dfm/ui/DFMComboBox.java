package org.duckdns.hjow.dfm.ui;
/*
Copyright 2026 HJOW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import java.util.Vector;

import javax.swing.JComboBox;

/** JComboBox 의 Warning (자바7로 오면서 제네릭이 추가됨으로 인함) 제거용 상속체 */
public class DFMComboBox extends JComboBox<ComboBoxItem> {
    private static final long serialVersionUID = 7972564005423012085L;
    public DFMComboBox() {
        super();
    }
    
    public DFMComboBox(Vector<ComboBoxItem> items) {
        super(items);
    }
}
