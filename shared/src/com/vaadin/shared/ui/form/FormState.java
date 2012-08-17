/* 
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.shared.ui.form;

import com.vaadin.shared.AbstractFieldState;
import com.vaadin.shared.Connector;

public class FormState extends AbstractFieldState {
    private Connector layout;
    private Connector footer;

    public Connector getLayout() {
        return layout;
    }

    public void setLayout(Connector layout) {
        this.layout = layout;
    }

    public Connector getFooter() {
        return footer;
    }

    public void setFooter(Connector footer) {
        this.footer = footer;
    }

}